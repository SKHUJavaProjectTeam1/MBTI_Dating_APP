package com.mbtidating.handler;

import com.mbtidating.dto.User;
import jakarta.websocket.Session;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MatchQueueManager {

    private final MatchStrategy matchStrategy;
    private final Map<Session, User> waitingUsers = new ConcurrentHashMap<>();

    public MatchQueueManager(MatchStrategy strategy) {
        this.matchStrategy = strategy;
    }

    public synchronized void addToQueue(User user, Session session) {
        waitingUsers.put(session, user);
        System.out.println("대기열 등록: " + user.getUserName() + " (" + user.getGender() + ")");
        tryMatch(user, session);
    }

    private void tryMatch(User me, Session mySession) {
        List<User> candidates = new ArrayList<>(waitingUsers.values());
        User match = matchStrategy.findMatch(me, candidates);

        if (match != null) {
            Session partnerSession = waitingUsers.entrySet().stream()
                    .filter(e -> e.getValue().equals(match))
                    .map(Map.Entry::getKey)
                    .findFirst().orElse(null);

            if (partnerSession != null) {
                mySession.getAsyncRemote().sendText("{\"type\":\"match_found\", \"partner\":\"" + match.getUserName() + "\"}");
                partnerSession.getAsyncRemote().sendText("{\"type\":\"match_found\", \"partner\":\"" + me.getUserName() + "\"}");
                waitingUsers.remove(mySession);
                waitingUsers.remove(partnerSession);

                System.out.println("매칭 완료: " + me.getUserName() + " <-> " + match.getUserName());
            }
        }
    }

    public void removeSession(Session session) {
        waitingUsers.remove(session);
    }
}
