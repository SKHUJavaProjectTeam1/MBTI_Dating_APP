package com.mbtidating.handler;

import com.mbtidating.config.JwtUtil;
import com.mbtidating.dto.User;
import com.mbtidating.repository.UserRepository;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@ServerEndpoint("/ws/match/{token}")
public class MatchSocketHandler {

    // 대기 중인 유저 이름(중복 접속 방지용)
    private static final Set<String> waitingUsers = ConcurrentHashMap.newKeySet();

    // 대기 큐 (세션 순서)
    private static final Queue<Session> queue = new ConcurrentLinkedQueue<>();

    // UserRepository를 static으로 보관 (ServerEndpoint 때문)
    private static UserRepository staticUserRepo;

    // 매칭 주기 (ms)
    private static final long MATCH_INTERVAL_MS = 3500L;

    @Autowired
    public void setUserRepo(UserRepository repo) {
        MatchSocketHandler.staticUserRepo = repo;
    }

    // ============================================================
    // 1) 매칭 스케줄러: 3.5초마다 tryMatchAll 실행
    // ============================================================
    static {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(MATCH_INTERVAL_MS);
                    try {
                        tryMatchAll();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    // 서버 내려갈 때 인터럽트 들어오면 안전하게 종료
                    break;
                }
            }
        }, "match-scheduler-thread");
        t.setDaemon(true); // 데몬 스레드로
        t.start();
    }

    // ============================================================
    // 2) 클라이언트 접속
    // ============================================================
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {

        String username = JwtUtil.validateToken(token); // username = loginId(userId)

        if (username == null) {
            session.close();
            return;
        }

        // 이미 대기중인 유저면 중복 접속 막기
        if (!waitingUsers.add(username)) {
            session.close();
            return;
        }

        if (staticUserRepo == null) {
            // Repository 주입 안 된 상태이면 방어
            session.close();
            waitingUsers.remove(username);
            return;
        }

        // loginId로 조회
        User user = staticUserRepo.findByLoginId(username).orElse(null);

        if (user == null) {
            session.close();
            waitingUsers.remove(username);
            return;
        }

        session.getUserProperties().put("username", username);
        session.getUserProperties().put("user", user);

        queue.add(session);

        System.out.println("[MATCH] 새 요청 → " + username);
        // 여기서는 즉시 tryMatch 호출 안 함 (딜레이는 스케줄러에서만)
    }

    // ============================================================
    // 3) 배치 매칭: 큐를 돌면서 계속 짝을 찾음
    // ============================================================
    private static synchronized void tryMatchAll() throws IOException {
        // 0. 먼저 닫힌 세션/깨진 세션 정리
        cleanupClosedSessions();

        if (queue.size() < 2) return;

        // 무한루프 방지용 가드
        int loopGuard = queue.size() * 2;

        while (queue.size() >= 2 && loopGuard-- > 0) {

            Session meSession = queue.poll();
            if (meSession == null) continue;

            if (!meSession.isOpen()) {
                removeSession(meSession);
                continue;
            }

            User me = (User) meSession.getUserProperties().get("user");
            if (me == null) {
                removeSession(meSession);
                continue;
            }

            // 1) 현재 큐에 남아 있는 후보 세션들 수집
            List<Session> candidateSessions = new ArrayList<>();
            List<User> candidateUsers = new ArrayList<>();

            for (Session s : queue) {
                if (s == null || !s.isOpen() || s == meSession) continue;
                User u = (User) s.getUserProperties().get("user");
                if (u != null) {
                    candidateSessions.add(s);
                    candidateUsers.add(u);
                }
            }

            if (candidateUsers.isEmpty()) {
                // 나 말고 아무도 없으면 다시 뒤로 보내고 종료
                queue.add(meSession);
                break;
            }

            // 2) 매칭 전략 적용
            CompositeMatchStrategy strategy = new CompositeMatchStrategy()
                    .add(new MbtiScoreStrategy())
                    .add(new GenderScoreStrategy())
                    .add(new AgeScoreStrategy()) ;
            

            User best = strategy.findMatch(me, candidateUsers);

            if (best == null) {
                // 지금은 나랑 맞는 사람이 없는 경우 → 다시 뒤로 보내고 다음 턴
                queue.add(meSession);
                continue;
            }

            // 3) best User에 해당하는 Session 찾기
            Session bestSession = null;
            User bestUser = null;

            for (Session s : candidateSessions) {
                User u = (User) s.getUserProperties().get("user");
                if (u != null && u.getId().equals(best.getId()) && s.isOpen()) {
                    bestSession = s;
                    bestUser = u;
                    break;
                }
            }

            if (bestSession == null || bestUser == null) {
                // 상대가 떠나버린 경우 → 나만 다시 대기열로
                queue.add(meSession);
                continue;
            }

            // 4) 큐에서 상대 제거
            queue.remove(bestSession);

            // 5) waitingUsers에서도 둘 다 제거
            waitingUsers.remove(me.getUserName());
            waitingUsers.remove(bestUser.getUserName());

            // 6) 매칭 결과 전송
            try {
                matchAndSend(meSession, bestSession, me, bestUser);
            } catch (IOException e) {
                // 전송 중 문제 생기면 둘 다 정리
                safeClose(meSession);
                safeClose(bestSession);
            }
        }
    }

    // 닫힌/깨진 세션 정리
    private static void cleanupClosedSessions() {
        for (Session s : new ArrayList<>(queue)) {
            if (s == null || !s.isOpen()) {
                removeSession(s);
            }
        }
    }

    // 특정 세션을 queue + waitingUsers에서 제거
    private static void removeSession(Session s) {
        if (s == null) return;
        Object nameObj = s.getUserProperties().get("username");
        if (nameObj instanceof String) {
            waitingUsers.remove((String) nameObj);
        }
        queue.remove(s);
        safeClose(s);
    }

    private static void safeClose(Session s) {
        if (s == null) return;
        try {
            if (s.isOpen()) s.close();
        } catch (Exception ignored) {
        }
    }

    // ============================================================
    // 4) 매칭 완료 시 결과 전송
    // ============================================================
    private static void matchAndSend(Session s1, Session s2, User me, User partner) throws IOException {

        String roomId = UUID.randomUUID().toString();
        System.out.println("[MATCH] 매칭 완료 → " + roomId + " / " + me.getUserName() + " - " + partner.getUserName());

        // s1에게 전송 (me 기준)
        send(s1, String.format(
                "{\"type\":\"match_found\", \"roomId\":\"%s\", \"partner\":\"%s\", \"self\":\"%s\", \"partnerName\":\"%s\", \"selfName\":\"%s\"}",
                roomId, partner.getId(), me.getId(), partner.getUserName(), me.getUserName()
        ));

        // s2에게 전송 (상대 기준)
        send(s2, String.format(
                "{\"type\":\"match_found\", \"roomId\":\"%s\", \"partner\":\"%s\", \"self\":\"%s\", \"partnerName\":\"%s\", \"selfName\":\"%s\"}",
                roomId, me.getId(), partner.getId(), me.getUserName(), partner.getUserName()
        ));

        safeClose(s1);
        safeClose(s2);
    }

    private static void send(Session s, String msg) throws IOException {
        if (s != null && s.isOpen()) {
            synchronized (s) {
                s.getBasicRemote().sendText(msg);
            }
        }
    }

    // ============================================================
    // 5) 클라이언트 종료 처리
    // ============================================================
    @OnClose
    public void onClose(Session session) {
        String username = (String) session.getUserProperties().get("username");

        if (username != null) {
            waitingUsers.remove(username);
        }

        queue.remove(session);
        safeClose(session);
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        System.out.println("[MATCH] WebSocket 오류: " + thr.getMessage());
        removeSession(session);
    }
}
