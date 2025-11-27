package com.mbtidating.view;

import com.mbtidating.dto.User;
import com.mbtidating.network.ApiClient;
import com.mbtidating.network.WebSocketClient;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject; // org.json를 사용하기 위해 pom.xml에 라이브러리 추가

public class ChatView extends JPanel {

    private final MainApp mainApp;
    private WebSocketClient socketClient;
    private String roomId;
    private String userName;   // ← 현재 로그인한 아이디(id)

    // 왼쪽 리스트용
    private final DefaultListModel<RoomItem> roomListModel = new DefaultListModel<>();
    private final JList<RoomItem> roomList = new JList<>(roomListModel);

    private final Color colorMy = new Color(200, 255, 230);
    private final Color colorOther = new Color(255, 189, 189);
    private final Color colorTop = new Color(189, 255, 243);

    private final JPanel messageArea = new JPanel();
    private final JTextField inputField = new JTextField();
    private final JButton sendButton = new JButton();
    private final JLabel topNameLabel = new JLabel("채팅 중...", SwingConstants.LEFT);

    public ChatView(MainApp mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(buildLeftPanel(), BorderLayout.WEST);
        add(buildRightPanel(), BorderLayout.CENTER);
    }

    // ============================ 왼쪽: 채팅방 리스트 ============================

    private JPanel buildLeftPanel() {
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, colorOther, 0, getHeight(), colorTop));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        leftPanel.setPreferredSize(new Dimension(250, 760));
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("채팅방 목록", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        leftPanel.add(titleLabel, BorderLayout.NORTH);

        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomList.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        // 리스트 더블클릭 시 해당 방 입장
        roomList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    RoomItem item = roomList.getSelectedValue();
                    if (item != null) {
                        startChat(item.roomId, userName);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(roomList);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        leftPanel.add(scroll, BorderLayout.CENTER);

        // 새로고침 버튼
        JButton refreshBtn = new JButton("새로고침");
        refreshBtn.addActionListener(e -> refreshRoomList());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        bottom.setOpaque(false);
        bottom.add(refreshBtn);
        leftPanel.add(bottom, BorderLayout.SOUTH);

        return leftPanel;
    }

    // 채팅방 한 개를 표현하는 아이템
    private static class RoomItem {
        final String roomId;
        final String title;   // 상대 아이디 또는 방 이름

        RoomItem(String roomId, String title) {
            this.roomId = roomId;
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    // 현재 로그인 유저 기준으로 내 채팅방 목록 갱신
    public void refreshRoomList() {
        try {
            User u = mainApp.getLoggedInUser();
            if (u == null) return;

            this.userName = u.getId();   // 로그인 아이디 사용 (participants.userId와 동일)

            ApiClient.HttpResult res = ApiClient.get("/chat/rooms/" + userName);
            if (!res.isOk() || res.body == null || res.body.isEmpty()) {
                roomListModel.clear();
                return;
            }

            JSONArray arr = new JSONArray(res.body);
            roomListModel.clear();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject room = arr.getJSONObject(i);
                String rId = room.getString("roomId");

                // 참가자 중 나 말고 상대 아이디 찾기
                String partner = "(상대 없음)";
                if (room.has("participants")) {
                    JSONArray ps = room.getJSONArray("participants");
                    for (int j = 0; j < ps.length(); j++) {
                        JSONObject p = ps.getJSONObject(j);
                        String uid = p.optString("userId", "");
                        if (!uid.isEmpty() && !uid.equals(userName)) {
                            partner = uid;
                            break;
                        }
                    }
                }

                RoomItem item = new RoomItem(rId, partner + " 님과의 대화");
                roomListModel.addElement(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================ 오른쪽: 채팅 화면 ============================

    private JPanel buildRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 상단 박스 (방 제목/프로필)
        JPanel topBox = new JPanel(new BorderLayout());
        topBox.setBackground(colorTop);
        topBox.setPreferredSize(new Dimension(200, 60));

        JLabel avatar = new JLabel(new ImageIcon("images/default_profile.png"));
        avatar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topBox.add(avatar, BorderLayout.WEST);

        topNameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        topBox.add(topNameLabel, BorderLayout.CENTER);

        JButton homeButton = new JButton("홈으로");
        homeButton.setFocusPainted(false);
        homeButton.addActionListener(e -> {
            closeChat();
            mainApp.showView(MainApp.HOME);
        });
        topBox.add(homeButton, BorderLayout.EAST);

        rightPanel.add(topBox, BorderLayout.NORTH);

        // 메시지 영역
        messageArea.setBackground(new Color(255, 240, 240));
        messageArea.setLayout(new BoxLayout(messageArea, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(messageArea);
        scroll.setBorder(null);
        rightPanel.add(scroll, BorderLayout.CENTER);

        // 입력 박스
        JPanel bottomBox = new JPanel();
        bottomBox.setBackground(new Color(230, 230, 230));
        bottomBox.setLayout(new BoxLayout(bottomBox, BoxLayout.X_AXIS));
        bottomBox.setPreferredSize(new Dimension(0, 70));

        inputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        inputField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK));
        bottomBox.add(inputField);
        bottomBox.add(Box.createRigidArea(new Dimension(5, 0)));

        sendButton.setIcon(new ImageIcon("images/submit.png"));
        sendButton.setPreferredSize(new Dimension(50, 50));
        sendButton.setFocusPainted(false);
        sendButton.setBackground(Color.WHITE);
        bottomBox.add(sendButton);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        rightPanel.add(bottomBox, BorderLayout.SOUTH);

        return rightPanel;
    }

    // ============================ 채팅방 입장 ============================

    public void startChat(String roomId, String userName) {
        this.roomId = roomId;
        this.userName = userName;

        try {
            // ---- 0. 기존 메시지 제거 ----
            messageArea.removeAll();
            refreshMessages();

            // ---- 1. 과거 메시지 로드 ----
            loadChatHistory();

            // ---- 2. WebSocket 연결 ----
            String encodedUser = URLEncoder.encode(userName, StandardCharsets.UTF_8.toString());
            String wsUrl = "ws://localhost:8080/ws/chat/" + roomId + "/" + encodedUser;

            socketClient = new WebSocketClient(wsUrl, userName);
            socketClient.onMessage(msg -> SwingUtilities.invokeLater(() -> receiveMessage(msg)));
            socketClient.connect();

            // ---- 3. 상단 타이틀 업데이트 ----
            topNameLabel.setText("채팅방: " + roomId.substring(0, 6) + "...");

            // ---- 4. 시스템 메시지 ----
            addSystemMessage("채팅방에 입장했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================ DB 기록 불러오기 ============================

    private void loadChatHistory() {
        try {
            ApiClient.HttpResult result = ApiClient.get("/chat/" + roomId);

            if (result == null || result.body == null || result.body.isEmpty()) return;

            String json = result.body;
            JSONObject root = new JSONObject(json);

            if (!root.has("chatHistory")) return;

            JSONArray arr = root.getJSONArray("chatHistory");

            for (int i = 0; i < arr.length(); i++) {
                JSONObject m = arr.getJSONObject(i);
                String sender = m.getString("senderId");
                String text = m.getString("message");

                if (sender.equals(userName)) {
                    addMyMessage(text);
                } else {
                    addOtherMessage(sender + ": " + text);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================ 메시지 처리 ============================

    private boolean sending = false;

    private synchronized void sendMessage() {
        if (sending) return;
        sending = true;

        String msg = inputField.getText().trim();

        if (!msg.isEmpty() && socketClient != null) {
            socketClient.send(msg);
            addMyMessage(msg);
            inputField.setText("");
        }

        sending = false;
    }

    private void receiveMessage(String msg) {
        JSONObject data = null;
        boolean isJson = true;

        // JSON 파싱 시도
        try {
            data = new JSONObject(msg);
        } catch (Exception e) {
            isJson = false;
        }

        if (isJson && data != null) {
            // JSON 기반 메시지 처리
            String type = data.optString("type", "");
            switch (type) {
                case "chat":
                    String sender = data.optString("sender", "unknown");
                    String message = data.optString("message", "");
                    addOtherMessage(sender + ": " + message);
                    break;
                case "system":
                    String sysMessage = data.optString("message", "");
                    addSystemMessage(sysMessage);
                    break;
                default:
                    // 알 수 없는 타입 처리
                    addSystemMessage("알 수 없는 메시지 타입: " + msg);
            }
        } else {
            // 기존 문자열 호환 처리
            if (msg.startsWith("🔔") || msg.startsWith("❌")) {
                addSystemMessage(msg);
            } else if (msg.contains(": ")) {
                addOtherMessage(msg);
            }
        }
    }

    // ============================ 메시지 UI ============================

    private void addMyMessage(String msg) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);

        JLabel label = new JLabel(msg);
        label.setOpaque(true);
        label.setBackground(colorMy);
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        panel.add(label);
        messageArea.add(panel);
        messageArea.add(Box.createVerticalStrut(8));
        refreshMessages();
    }

    private void addOtherMessage(String msg) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);

        JLabel label = new JLabel(msg);
        label.setOpaque(true);
        label.setBackground(colorOther);
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        panel.add(label);
        messageArea.add(panel);
        messageArea.add(Box.createVerticalStrut(8));
        refreshMessages();
    }

    private void addSystemMessage(String msg) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setOpaque(false);

        JLabel label = new JLabel(msg);
        label.setFont(new Font("맑은 고딕", Font.ITALIC, 13));

        panel.add(label);
        messageArea.add(panel);
        messageArea.add(Box.createVerticalStrut(8));
        refreshMessages();
    }

    private void refreshMessages() {
        messageArea.revalidate();
        messageArea.repaint();
    }

    // ============================ 종료 처리 ============================

    public void closeChat() {
        if (socketClient != null) {
            socketClient.close();
            addSystemMessage("채팅방을 나갔습니다.");
        }
    }
}
