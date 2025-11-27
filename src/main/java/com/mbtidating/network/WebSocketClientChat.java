package com.mbtidating.network;

import jakarta.websocket.*;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

@ClientEndpoint
public class WebSocketClientChat {

    private Session session;
    private final String endpointUrl;
    private Consumer<JSONObject> jsonHandler;

    public WebSocketClientChat(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    // JSON 핸들러 등록
    public void onJson(Consumer<JSONObject> handler) {
        this.jsonHandler = handler;
    }

    // WebSocket 연결
    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, URI.create(endpointUrl));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 연결됨
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("[CHAT CLIENT] Connected → " + endpointUrl);
    }

    // 메시지 수신(JSON)
    @OnMessage
    public void onMessage(String message) {
        System.out.println("[CHAT CLIENT] Message: " + message);

        if (jsonHandler == null) return;

        try {
            JSONObject json = new JSONObject(message);
            jsonHandler.accept(json);
        } catch (Exception e) {
            System.out.println("[CHAT CLIENT] Non-JSON Message: " + message);
        }
    }

    // 메시지 전송
    public void sendChat(String senderId, String senderName, String content) {
        try {
            if (session == null || !session.isOpen()) {
                System.err.println("⚠️ [CHAT CLIENT] 세션이 닫혀 전송 불가");
                return;
            }

            JSONObject root = new JSONObject();
            root.put("type", "CHAT");

            JSONObject data = new JSONObject();
            data.put("senderId", senderId);
            data.put("senderName", senderName);
            data.put("content", content);

            root.put("data", data);

            synchronized (session) {
                session.getBasicRemote().sendText(root.toString());
            }

        } catch (IOException e) {
            System.err.println("⚠️ [CHAT CLIENT] 전송 실패: " + e.getMessage());
        }
    }

    // 연결 종료
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("[CHAT CLIENT] Closed: " + reason);
    }

    // 에러 처리
    @OnError
    public void onError(Session session, Throwable t) {
        System.err.println("[CHAT CLIENT] Error: " + t.getMessage());
    }

    public void close() {
        try {
            if (session != null) {
                session.close();
                session = null;
            }
            System.out.println("[CHAT CLIENT] Closed manually.");
        } catch (Exception ignored) {}
    }
}
