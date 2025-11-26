package com.mbtidating.handler;

import com.mbtidating.config.JwtUtil;
import com.mbtidating.dto.User;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.mbtidating.repository.UserRepository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@ServerEndpoint("/ws/match/{token}")
public class MatchSocketHandler {

    private static final Set<String> waitingUsers = ConcurrentHashMap.newKeySet();
    private static final Queue<Session> queue = new ConcurrentLinkedQueue<>();
    private static UserRepository staticUserRepo;

    @Autowired
    public void setUserRepo(UserRepository repo) {
        MatchSocketHandler.staticUserRepo = repo;
    }
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {

        String username = JwtUtil.validateToken(token); // username = loginId(userId)

        if (username == null) {
            session.close();
            return;
        }

        if (!waitingUsers.add(username)) {
            session.close();
            return;
        }

        // ★ loginId로 조회해야 함
        User user = staticUserRepo.findByLoginId(username).orElse(null);

        if (user == null) {
            session.close();
            return;
        }

        session.getUserProperties().put("username", username); // loginId
        session.getUserProperties().put("user", user);         // User 객체 전체

        queue.add(session);

        System.out.println("[MATCH] 새 요청 → " + username);

        tryMatch();
    }




    private synchronized void tryMatch() throws IOException {

        if (queue.size() < 2) return;

        // 큐에서 "가장 앞에 있는 session"을 me 로 설정
        Session meSession = queue.peek();
        if (meSession == null) return;

        User me = (User) meSession.getUserProperties().get("user");
        if (me == null) return;

        // 후보 목록 만들기 (자기 자신 제외)
        List<User> candidates = new ArrayList<>();
        for (Session s : queue) {
            if (s == meSession) continue;
            User u = (User) s.getUserProperties().get("user");
            if (u != null) candidates.add(u);
        }

        // 매칭 전략 준비
        CompositeMatchStrategy strategy = new CompositeMatchStrategy()
                .add(new MbtiScoreStrategy())
                .add(new GenderScoreStrategy());

        // 최고의 상대 찾기
        User best = strategy.findMatch(me, candidates);
        if (best == null) return;

        // best session 찾기
        Session bestSession = null;
        for (Session s : queue) {
            User u = (User) s.getUserProperties().get("user");
            if (u != null && u.getUserName().equals(best.getUserName())) {
                bestSession = s;
                break;
            }
        }

        if (bestSession == null) return;

        // 매칭된 두 세션 제거
        queue.remove(meSession);
        queue.remove(bestSession);

        waitingUsers.remove(me.getUserName());
        waitingUsers.remove(best.getUserName());

        // 룸 생성
        String roomId = UUID.randomUUID().toString();
        System.out.println("[MATCH] 매칭 완료 → " + roomId + " / " + me.getUserName() + " - " + best.getUserName());

        // 결과 전송
        send(meSession, String.format(
                "{\"type\":\"match_found\", " +
                        "\"roomId\":\"%s\", " +
                        "\"partner\":\"%s\", " +
                        "\"self\":\"%s\", " +
                        "\"partnerName\":\"%s\", " +
                        "\"selfName\":\"%s\"}",
                roomId,
                best.getId(), me.getId(),
                best.getUserName(), me.getUserName()
        ));


        send(bestSession, String.format(
                "{\"type\":\"match_found\", " +
                        "\"roomId\":\"%s\", " +
                        "\"partner\":\"%s\", " +
                        "\"self\":\"%s\", " +
                        "\"partnerName\":\"%s\", " +
                        "\"selfName\":\"%s\"}",
                roomId,
                me.getId(), best.getId(),
                me.getUserName(), best.getUserName()
        ));



        meSession.close();
        bestSession.close();
    }


    private void send(Session s, String msg) throws IOException {
        if (s.isOpen()) {
            synchronized (s) {
                s.getBasicRemote().sendText(msg);
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        String username = (String) session.getUserProperties().get("username");

        if (username != null) {
            waitingUsers.remove(username);
        }

        queue.remove(session);
    }

}
