package com.mbtidating.view;

import com.mbtidating.network.WebSocketClientMatch;
import javax.swing.*;
import java.awt.*;
import org.json.JSONObject;

public class MatchWaitView extends JPanel {

    private final MainApp mainApp;
    private WebSocketClientMatch socketClient;

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

        socketClient = new WebSocketClientMatch(
                "ws://localhost:8080/ws/match/" + jwtToken,
                jwtToken
        );

        socketClient.onMessage(msg -> {
            try {
                JSONObject json = new JSONObject(msg);

                // 🔥 서버에서 보내는 타입은 match_found
                if (!json.optString("type").equals("match_found"))
                    return;

                // 🔥 서버 JSON은 data 객체가 없고, 최상단에 바로 존재함
                String roomId = json.getString("roomId");
                String selfId = json.getString("self");
                String selfName = json.optString("selfName", selfId);
                String partnerId = json.getString("partner");
                String partnerName = json.optString("partnerName", partnerId);

                System.out.println("[MATCH] 매칭 성공 → "
                        + roomId + " / " + selfName + " - " + partnerName);

                SwingUtilities.invokeLater(() -> {
                    mainApp.setMatched(true);

                    ChatView chatView = mainApp.getChatView();
                    chatView.startChat(roomId, selfId, selfName, partnerId, partnerName);

                    mainApp.showView(MainApp.CHAT);
                });

            } catch (Exception e) {
                System.err.println("[MATCH ERROR] JSON 파싱 실패: " + msg);
                e.printStackTrace();
            }
        });

        socketClient.connect();
    }

    private void cancelMatch() {
        if (socketClient != null) socketClient.close();
        mainApp.showView(MainApp.HOME);
    }
}
