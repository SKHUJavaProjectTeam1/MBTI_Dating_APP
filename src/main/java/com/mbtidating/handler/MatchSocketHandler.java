package com.mbtidating.handler;

import com.mbtidating.config.JwtUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Queue;
import java.util.UUID;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@ServerEndpoint("/ws/match/{token}")
public class MatchSocketHandler {

    private static final Set<String> waitingUsers = ConcurrentHashMap.newKeySet();
    private static final Queue<Session> queue = new ConcurrentLinkedQueue<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {

        String username = JwtUtil.validateToken(token);

        if (username == null) {
            session.close();
            return;
        }

        // 중복 진입 방지
        if (!waitingUsers.add(username)) {
            System.out.println("[MATCH] 이미 대기중이므로 거절 → " + username);
            session.close();
            return;
        }

        session.getUserProperties().put("username", username);

        queue.add(session);
        System.out.println("[MATCH] 새 요청 → " + username);

        tryMatch();
    }

    private void tryMatch() throws IOException {
        if (queue.size() < 2) return;

        Session s1 = queue.poll();
        Session s2 = queue.poll();

        if (s1 == null || s2 == null) return;

        String u1 = (String) s1.getUserProperties().get("username");
        String u2 = (String) s2.getUserProperties().get("username");

        // 대기열에서 제거
        waitingUsers.remove(u1);
        waitingUsers.remove(u2);

        String roomId = UUID.randomUUID().toString();

        System.out.println("[MATCH] 매칭 완료 → room=" + roomId + " / " + u1 + " - " + u2);

        send(s1, String.format(
                "{\"type\":\"match_found\",\"roomId\":\"%s\",\"partner\":\"%s\",\"self\":\"%s\"}",
                roomId, u2, u1));

        send(s2, String.format(
                "{\"type\":\"match_found\",\"roomId\":\"%s\",\"partner\":\"%s\",\"self\":\"%s\"}",
                roomId, u1, u2));

        s1.close();
        s2.close();
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
        waitingUsers.remove(username);
        queue.remove(session);
    }
}
