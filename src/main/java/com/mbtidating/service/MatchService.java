package com.mbtidating.service;

import com.mbtidating.dto.User;
import com.mbtidating.handler.CompositeMatchStrategy;
import com.mbtidating.handler.GenderScoreStrategy;
import com.mbtidating.handler.MbtiScoreStrategy;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
public class MatchService {

    // userKey -> WebSocket Session
    private final Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    // userKey -> User
    private final Map<String, User> userMap = new ConcurrentHashMap<>();
    // 대기열: userKey 순서
    private final Queue<String> queue = new ConcurrentLinkedQueue<>();

    // 이미 매칭된 사용자 기록 (메모리 기반, 같은 둘이 다시 매칭되는 것 방지)
    private final Map<String, Set<String>> matchedHistory = new ConcurrentHashMap<>();

    // 마지막 매칭 시각 (재입장 쿨타임용)
    private final Map<String, Long> lastMatchTime = new ConcurrentHashMap<>();
    private static final long REJOIN_COOLDOWN_MS = 5000L; // 5초

    // 대기 보정 점수용
    private final Map<String, Integer> waitBonusCount = new ConcurrentHashMap<>();
    private static final int WAIT_BONUS_SCORE_PER_COUNT = 1;

    // 매칭 주기 (ms)
    private static final long MATCH_INTERVAL_MS = 3500L;

    /**
     * 유저가 매칭큐에 참여 (OnOpen에서 호출)
     * @return true면 큐에 참여, false면 거절(쿨타임 등)
     */
    public boolean join(User user, Session session) {
        if (user == null || session == null) return false;

        String userKey = getUserKey(user);
        long now = System.currentTimeMillis();
        Long last = lastMatchTime.get(userKey);

        log.info("[JOIN] 호출: userKey={} lastMatchTime={} now={}", userKey, last, now);

        // 재입장 쿨타임 체크
        if (last != null && (now - last) < REJOIN_COOLDOWN_MS) {
            log.info("[JOIN] ❌ 쿨타임 미충족: {} ({}), last={}, diff={}ms",
                    user.getUserName(), userKey, last, now - last);
            return false;
        }

        // 기존 세션이 남아 있다면 정리 (재접속 처리)
        Session oldSession = sessionMap.get(userKey);
        if (oldSession != null && oldSession != session) {
            log.info("[JOIN] 기존 세션 발견 → 정리: userKey={}", userKey);
            // 대기열, 맵에서 제거
            queue.remove(userKey);
            sessionMap.remove(userKey);
            userMap.remove(userKey);
            safeClose(oldSession);
        }

        // 새 세션 등록
        sessionMap.put(userKey, session);
        userMap.put(userKey, user);

        // leave()에서 쓸 수 있도록 세션 속성에 userKey 저장 (치명 버그 fix)
        session.getUserProperties().put("userKey", userKey);
        session.getUserProperties().put("user", user);

        // 대기열 중복 방지 후 추가
        if (!queue.contains(userKey)) {
            queue.add(userKey);
        }

        log.info("[JOIN] 성공: {} 큐 참여 완료 / 현재 queue={}", userKey, queue);
        return true;
    }

    /**
     * 유저가 연결 종료 / 에러 발생 시 호출 (OnClose/OnError)
     */
    public void leave(Session session) {
        if (session == null) return;
        Object keyObj = session.getUserProperties().get("userKey");
        if (!(keyObj instanceof String userKey)) {
            safeClose(session);
            return;
        }

        // 큐 / 맵 동기 제거
        queue.remove(userKey);
        sessionMap.remove(userKey);
        userMap.remove(userKey);

        safeClose(session);

        log.info("[MATCH] 큐 이탈 → {}", userKey);
    }

    /**
     * 매칭 스케줄러
     * - SpringBoot 메인 클래스에 @EnableScheduling 필요
     */
    @Scheduled(fixedDelay = MATCH_INTERVAL_MS)
    public synchronized void tryMatchAll() {
        log.info("[MATCH] 🔔 scheduler tick! queue size = {}", queue.size());
        cleanupClosedSessions();

        if (queue.size() < 2) return;

        int loopGuard = queue.size() * 2;

        while (queue.size() >= 2 && loopGuard-- > 0) {

            String meKey = queue.poll();
            if (meKey == null) continue;

            Session meSession = sessionMap.get(meKey);
            User me = userMap.get(meKey);

            if (meSession == null || !meSession.isOpen() || me == null) {
                removeByKey(meKey);
                continue;
            }

            // 후보 수집
            List<String> candidateKeys = new ArrayList<>(queue);
            if (candidateKeys.isEmpty()) {
                queue.add(meKey);
                increaseWaitBonus(meKey);
                break;
            }

            CompositeMatchStrategy strategy = new CompositeMatchStrategy()
                    .add(new MbtiScoreStrategy())
                    .add(new GenderScoreStrategy());

            String bestKey = null;
            User bestUser = null;
            Session bestSession = null;
            int bestScore = Integer.MIN_VALUE;

            for (String cKey : candidateKeys) {
                if (hasMatchedBefore(meKey, cKey)) continue;

                Session cSession = sessionMap.get(cKey);
                User cUser = userMap.get(cKey);

                if (cSession == null || !cSession.isOpen() || cUser == null) continue;

                int baseScore = strategy.calculateScore(me, cUser);
                int waitBonus = getWaitBonus(cKey) * WAIT_BONUS_SCORE_PER_COUNT;
                int totalScore = baseScore + waitBonus;

                if (totalScore > bestScore) {
                    bestScore = totalScore;
                    bestKey = cKey;
                    bestUser = cUser;
                    bestSession = cSession;
                }
            }

            if (bestKey == null || bestUser == null || bestSession == null) {
                // 매칭 가능한 후보 없음 → 다시 큐 뒤로 + 대기 보너스
                queue.add(meKey);
                increaseWaitBonus(meKey);
                continue;
            }

            // 상대가 여전히 유효한지 최종 체크
            if (!bestSession.isOpen()) {
                queue.add(meKey);
                increaseWaitBonus(meKey);
                continue;
            }

            // 큐에서 두 명 제거
            queue.remove(bestKey);

            // 매칭 이력 기록
            recordMatch(meKey, bestKey);

            // 대기 보너스 / 마지막 매칭 시각 갱신
            resetWaitBonus(meKey);
            resetWaitBonus(bestKey);
            long now = System.currentTimeMillis();
            lastMatchTime.put(meKey, now);
            lastMatchTime.put(bestKey, now);

            // 매칭 결과 전송 (여기서는 close 하지 않음!)
            try {
                sendMatch(meSession, bestSession, me, bestUser);
            } catch (IOException e) {
                log.warn("[MATCH] 매칭 결과 전송 중 오류: {}", e.getMessage());
                safeClose(meSession);
                safeClose(bestSession);
            }
        }
    }

    // ===============================================
    // 내부 유틸
    // ===============================================

    private void cleanupClosedSessions() {
        for (String key : new ArrayList<>(queue)) {
            Session s = sessionMap.get(key);
            if (s == null || !s.isOpen()) {
                removeByKey(key);
            }
        }
    }

    private void removeByKey(String userKey) {
        queue.remove(userKey);
        Session s = sessionMap.remove(userKey);
        userMap.remove(userKey);
        safeClose(s);
    }

    private void safeClose(Session s) {
        if (s == null) return;
        try {
            if (s.isOpen()) s.close();
        } catch (Exception ignored) {
        }
    }

    private void sendMatch(Session s1, Session s2, User me, User partner) throws IOException {
        String roomId = UUID.randomUUID().toString();
        log.info("[MATCH] 매칭 완료 → {} / {} - {}", roomId, me.getUserName(), partner.getUserName());

        send(s1, String.format(
                "{\"type\":\"match_found\", \"roomId\":\"%s\", \"partner\":\"%s\", \"self\":\"%s\", \"partnerName\":\"%s\", \"selfName\":\"%s\"}",
                roomId, partner.getId(), me.getId(), partner.getUserName(), me.getUserName()
        ));

        send(s2, String.format(
                "{\"type\":\"match_found\", \"roomId\":\"%s\", \"partner\":\"%s\", \"self\":\"%s\", \"partnerName\":\"%s\", \"selfName\":\"%s\"}",
                roomId, me.getId(), partner.getId(), me.getUserName(), partner.getUserName()
        ));
        // 여기서는 close 하지 않는다. 클라이언트가 채팅방으로 전환하면서 스스로 close 하게 두는 게 안전함.
    }

    private void send(Session s, String msg) throws IOException {
        if (s != null && s.isOpen()) {
            synchronized (s) {
                s.getBasicRemote().sendText(msg);
            }
        }
    }

    // ===========================
    // 매칭 이력 / 대기 보너스
    // ===========================

    private String getUserKey(User u) {
        if (u == null) return null;
        if (u.getId() != null) {
            return u.getId().toString();
        }
        return u.getUserName();
    }

    private boolean hasMatchedBefore(String keyA, String keyB) {
        if (keyA == null || keyB == null) return false;
        Set<String> set = matchedHistory.get(keyA);
        return set != null && set.contains(keyB);
    }

    private void recordMatch(String keyA, String keyB) {
        if (keyA == null || keyB == null) return;
        matchedHistory
                .computeIfAbsent(keyA, k -> ConcurrentHashMap.newKeySet())
                .add(keyB);
        matchedHistory
                .computeIfAbsent(keyB, k -> ConcurrentHashMap.newKeySet())
                .add(keyA);
    }

    private void increaseWaitBonus(String userKey) {
        if (userKey == null) return;
        waitBonusCount.merge(userKey, 1, Integer::sum);
    }

    private void resetWaitBonus(String userKey) {
        if (userKey == null) return;
        waitBonusCount.remove(userKey);
    }

    private int getWaitBonus(String userKey) {
        if (userKey == null) return 0;
        return waitBonusCount.getOrDefault(userKey, 0);
    }
}
