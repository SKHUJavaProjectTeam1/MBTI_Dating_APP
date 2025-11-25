package com.mbtidating.network;

import jakarta.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

@ClientEndpoint
public class WebSocketClient {

    public enum Type { MATCH, CHAT }

    private Type type;
    private Session session;
    private final String endpointUrl;
    private final String token;
    private Consumer<String> messageHandler;

    public WebSocketClient(String endpointUrl, String token, Type type) {
        this.endpointUrl = endpointUrl;
        this.token = token;
        this.type = type;
    }

    public WebSocketClient(String endpointUrl, String token) {
        this(endpointUrl, token, Type.MATCH);
    }

    public void onMessage(Consumer<String> handler) {
        this.messageHandler = handler;
    }

    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            // ❗ Query param 제거
            container.connectToServer(this, URI.create(endpointUrl));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("[CLIENT] Connected to " + endpointUrl);

        // ❗ enqueue 자동 전송 완전 제거
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("[CLIENT] Message: " + message);
        if (messageHandler != null) messageHandler.accept(message);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("[CLIENT] Closed: " + reason);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        System.err.println("[CLIENT] Error: " + t.getMessage());
    }

    public void send(String msg) {
        try {
            if (session != null && session.isOpen()) {
                synchronized (session) {
                    session.getBasicRemote().sendText(msg);
                }
            } else {
                System.err.println("⚠️ [Client] 세션이 닫혀 전송 불가");
            }
        } catch (Exception e) {
            System.err.println("⚠️ [Client] 전송 실패: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (session != null) {
                session.close();
                session = null;  // ❗ 필수
            }
            System.out.println("[CLIENT] Connection closed manually.");
        } catch (Exception ignored) {}
    }
}
