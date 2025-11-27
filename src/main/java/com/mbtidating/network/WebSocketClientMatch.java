package com.mbtidating.network;

import jakarta.websocket.*;
import java.net.URI;
import java.util.function.Consumer;

@ClientEndpoint
public class WebSocketClientMatch {

    private Session session;
    private final String endpointUrl;
    private final String token;
    private Consumer<String> messageHandler;

    public WebSocketClientMatch(String endpointUrl, String token) {
        this.endpointUrl = endpointUrl;
        this.token = token;
    }

    public void onMessage(Consumer<String> handler) {
        this.messageHandler = handler;
    }

    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, URI.create(endpointUrl));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("[MATCH CLIENT] Connected → " + endpointUrl);
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("[MATCH CLIENT] Message: " + message);
        if (messageHandler != null) messageHandler.accept(message);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("[MATCH CLIENT] Closed: " + reason);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        System.err.println("[MATCH CLIENT] Error: " + t.getMessage());
    }

    public void send(String msg) {
        try {
            if (session != null && session.isOpen()) {
                synchronized (session) {
                    session.getBasicRemote().sendText(msg);
                }
            } else {
                System.err.println("⚠️ [MATCH CLIENT] 세션이 닫혀 전송 불가");
            }
        } catch (Exception e) {
            System.err.println("⚠️ [MATCH CLIENT] 전송 실패: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (session != null) {
                session.close();
                session = null;
            }
            System.out.println("[MATCH CLIENT] Closed manually.");
        } catch (Exception ignored) {}
    }
}
