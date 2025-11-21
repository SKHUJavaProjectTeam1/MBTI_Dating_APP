package com.mbtidating.handler;

import com.mbtidating.dto.User;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MatchQueueManager {

    private final CompositeMatchStrategy matchStrategy; // ★ 변경됨!
    private final Map<Session, User> waitingUsers = new ConcurrentHashMap<>();

    public MatchQueueManager(CompositeMatchStrategy strategy) {
        this.matchStrategy = strategy;
    }

    public synchronized void addToQueue(User user, Session session) {
        waitingUsers.put(session, user);
        log.info("📥 대기열 등록: {} ({})", user.getUserName(), user.getGender());

        tryMatch(user, session);
    }

    private void tryMatch(User me, Session mySession) {

        List<User> candidates = new ArrayList<>(waitingUsers.values());

        log.info("🔍 매칭 시도: {} / 후보 {}명", me.getUserName(), candidates.size());

        User matched = matchStrategy.findMatch(me, candidates);

        if (matched == null) {
            log.info("⏳ 매칭 실패: {}", me.getUserName());
            return;
        }

        // 후보 User → Session 찾기
        Session partnerSession = waitingUsers.entrySet().stream()
                .filter(e -> e.getValue().equals(matched))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);

        if (partnerSession == null) {
            log.warn("⚠ 세션 못 찾음: {}", matched.getUserName());
            return;
        }

        // 매칭 성공 메시지 전달
        mySession.getAsyncRemote().sendText(
                "{\"type\":\"match_found\", \"partner\":\"" + matched.getUserName() + "\"}"
        );

        partnerSession.getAsyncRemote().sendText(
                "{\"type\":\"match_found\", \"partner\":\"" + me.getUserName() + "\"}"
        );

        // 대기열에서 제거
        waitingUsers.remove(mySession);
        waitingUsers.remove(partnerSession);

        log.info("💘 매칭 완료! {} ↔ {}", me.getUserName(), matched.getUserName());
    }

    public void removeSession(Session session) {
        waitingUsers.remove(session);
    }
}
