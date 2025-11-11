package com.mbtidating.view;

import com.mbtidating.network.WebSocketClient;
import javax.swing.*;
import java.awt.*;

public class MatchWaitView extends JPanel {

    private final MainApp mainApp;
    private WebSocketClient socketClient;

    private final JLabel statusLabel = new JLabel("매칭 중입니다...", SwingConstants.CENTER);
    private final JButton cancelButton = new JButton("취소");

    public MatchWaitView(MainApp mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        setBackground(new Color(255, 235, 235));

        statusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        add(statusLabel, BorderLayout.CENTER);

        cancelButton.setBackground(new Color(213, 201, 255));
        cancelButton.addActionListener(e -> cancelMatch());
        add(cancelButton, BorderLayout.SOUTH);
    }

    public void startMatching(String jwtToken) {
        socketClient = new WebSocketClient("ws://localhost:8080/ws/match/" + jwtToken, jwtToken);
        socketClient.onMessage(msg -> {
            if (msg.contains("match_found")) {
                String roomId = msg.split("\"roomId\":\"")[1].split("\"")[0];
                String partner = msg.split("\"partner\":\"")[1].split("\"")[0];
                String self = msg.split("\"self\":\"")[1].split("\"")[0];

                System.out.println("[MATCH] 매칭 성공: " + roomId + " / " + partner + " / " + self);

                SwingUtilities.invokeLater(() -> {
                    ChatView chatView = mainApp.getChatView();
                    chatView.startChat(roomId, self); // ✅ 하드코딩 금지
                    mainApp.showView(MainApp.CHAT);
                });
            }
        });

        socketClient.connect();
    }

    private void cancelMatch() {
        if (socketClient != null) socketClient.close();
        mainApp.showView(MainApp.HOME);
    }

    private String extractJsonField(String json, String key) {
        int i = json.indexOf("\"" + key + "\":\"");
        if (i == -1) return null;
        int start = i + key.length() + 4;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

}
