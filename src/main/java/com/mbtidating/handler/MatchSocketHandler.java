package com.mbtidating.handler;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;
import com.mbtidating.config.JwtUtil; // JWT 유틸 가져오기

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@ServerEndpoint("/ws/match/{token}")
public class MatchSocketHandler {

    private static final Queue<Session> waitingQueue = new ConcurrentLinkedQueue<>();
    private static final Map<Session, String> userMap = new HashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        String user = JwtUtil.validateToken(token);
        userMap.put(session, user);
        System.out.println("[WS] 연결됨: " + user);
    }

    @OnMessage
    public void onMessage(String msg, Session session) {
        System.out.println("[WS] 받은 메시지: " + msg);
        if (msg.contains("enqueue")) {
            waitingQueue.add(session);
            checkForMatch();
        }
    }

    private void checkForMatch() {
        if (waitingQueue.size() >= 2) {
            Session s1 = waitingQueue.poll();
            Session s2 = waitingQueue.poll();
            String user1 = userMap.get(s1);
            String user2 = userMap.get(s2);

            String roomId = UUID.randomUUID().toString().substring(0, 8);
            sendMessage(s1, "{\"type\":\"match_found\",\"roomId\":\"" + roomId + "\",\"partner\":\"" + user2 + "\",\"self\":\"" + user1 + "\"}");
            sendMessage(s2, "{\"type\":\"match_found\",\"roomId\":\"" + roomId + "\",\"partner\":\"" + user1 + "\",\"self\":\"" + user2 + "\"}");
            System.out.println("💘 매칭 완료! Room ID: " + roomId + " / " + user1 + " ↔ " + user2);
        }
    }

    private void sendMessage(Session s, String msg) {
        try {
            if (s.isOpen()) s.getBasicRemote().sendText(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        waitingQueue.remove(session);
        userMap.remove(session);
        System.out.println("[WS] 종료됨: " + reason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("[WS] 오류: " + throwable.getMessage());
    }
}
