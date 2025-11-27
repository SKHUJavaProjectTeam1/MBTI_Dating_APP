package com.mbtidating.view;

import com.mbtidating.dto.User;
import com.mbtidating.network.ApiClient;
import com.mbtidating.network.WebSocketClientChat;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ChatView extends JPanel {

    private final MainApp mainApp;
    private WebSocketClientChat socketClient;

    // 현재 채팅방 + 유저 정보
    private String roomId;
    private String userId;        // 로그인한 내 userId
    private String selfName;      // 내 닉네임
    private String partnerId;     // 상대 userId
    private String partnerName;   // 상대 닉네임

    // 왼쪽 리스트용
    private final DefaultListModel<RoomItem> roomListModel = new DefaultListModel<>();
    private final JList<RoomItem> roomList = new JList<>(roomListModel);

    // 색상 정의
    private final Color colorMy = new Color(200, 255, 230);
    private final Color colorOther = new Color(255, 189, 189);
    private final Color colorTop = new Color(189, 255, 243);

    // 오른쪽 채팅 UI 요소
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

    // ============================ 왼쪽: 채팅방 리스트 패널 ============================

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

        leftPanel.setPreferredSize(new Dimension(280, 760));
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("채팅방 목록", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        leftPanel.add(titleLabel, BorderLayout.NORTH);

        // 리스트 설정
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomList.setCellRenderer(new RoomListRenderer());
        roomList.setOpaque(false);
        roomList.setBackground(new Color(0, 0, 0, 0));
        roomList.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        roomList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    RoomItem item = roomList.getSelectedValue();
                    if (item != null) {
                        // 내 정보가 아직 세팅 안 되어 있으면 한번 더 가져오기
                        if (userId == null || selfName == null) {
                            User u = mainApp.getLoggedInUser();
                            if (u != null) {
                                userId = u.getId();
                                selfName = u.getUserName();
                            }
                        }

                        startChat(
                                item.roomId,
                                userId,
                                selfName,
                                item.partnerId,
                                item.partnerName
                        );
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(roomList);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        leftPanel.add(scroll, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("목록 새로고침");
        refreshBtn.setBackground(new Color(255, 255, 255, 200));
        refreshBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> refreshRoomList());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        bottom.setOpaque(false);
        bottom.add(refreshBtn);
        leftPanel.add(bottom, BorderLayout.SOUTH);

        return leftPanel;
    }

    // ============================ 리스트 셀 렌더러 ============================

    private class RoomListRenderer extends JPanel implements ListCellRenderer<RoomItem> {
        private final JLabel nameLabel = new JLabel();
        private final JLabel timeLabel = new JLabel();
        private final JLabel iconLabel = new JLabel();
        private boolean isSelected = false;

        public RoomListRenderer() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setOpaque(false);

            iconLabel.setPreferredSize(new Dimension(40, 40));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconLabel.setFont(new Font("Dialog", Font.BOLD, 16));
            iconLabel.setForeground(Color.WHITE);

            nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            nameLabel.setForeground(new Color(80, 80, 80));

            timeLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            timeLabel.setForeground(new Color(150, 150, 150));
            timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            add(iconLabel, BorderLayout.WEST);
            add(nameLabel, BorderLayout.CENTER);
            add(timeLabel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends RoomItem> list, RoomItem value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            this.isSelected = isSelected;

            String title = (value.partnerName != null) ? value.partnerName : "(상대 없음)" ;
            nameLabel.setText(title);

            String firstLetter = (!title.isEmpty()) ? title.substring(0, 1) : "?" ;
            iconLabel.setText(firstLetter);

            timeLabel.setText(getRelativeTime(value.lastMessageTime));

            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSelected) {
                g2.setColor(new Color(255, 255, 255, 230));
            } else {
                g2.setColor(new Color(255, 255, 255, 100));
            }
            g2.fillRoundRect(5, 2, getWidth() - 10, getHeight() - 4, 20, 20);

            g2.setColor(isSelected ? new Color(255, 189, 189) : new Color(100, 200, 200));
            g2.fillOval(10, 10, 40, 40);

            super.paintComponent(g);
            g2.dispose();
        }
    }

    // ============================ 말풍선 라벨 ============================

    private class BubbleLabel extends JLabel {
        private final Color bgColor;

        public BubbleLabel(String text, Color bgColor) {
            super(text);
            this.bgColor = bgColor;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            setForeground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.dispose();

            super.paintComponent(g);
        }
    }

    // ============================ 시간 포맷팅 유틸 ============================

    private String getRelativeTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) return "" ;

        try {
            LocalDateTime time = LocalDateTime.parse(isoDateTime, DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime now = LocalDateTime.now();

            long diffMinutes = ChronoUnit.MINUTES.between(time, now);
            long diffDays = ChronoUnit.DAYS.between(time.toLocalDate(), now.toLocalDate());

            if (diffMinutes < 1) return "방금 전" ;
            else if (diffMinutes < 60) return diffMinutes + "분 전" ;
            else if (diffDays == 0) return time.format(DateTimeFormatter.ofPattern("a h:mm"));
            else if (diffDays == 1) return "어제" ;
            else return time.format(DateTimeFormatter.ofPattern("M월 d일"));

        } catch (Exception e) {
            return "" ;
        }
    }

    // ============================ 채팅방 정보를 담는 아이템 ============================

    static class RoomItem {
        final String roomId;
        final String partnerId;
        final String partnerName;
        final String lastMessageTime;

        RoomItem(String roomId, String partnerId, String partnerName, String lastMessageTime) {
            this.roomId = roomId;
            this.partnerId = partnerId;
            this.partnerName = partnerName;
            this.lastMessageTime = lastMessageTime;
        }

        @Override
        public String toString() {
            return partnerName != null ? partnerName : "(상대 없음)" ;
        }
    }

    // ============================ 목록 갱신 ============================

    public void refreshRoomList() {
        try {
            User u = mainApp.getLoggedInUser();
            if (u == null) return;

            this.userId = u.getId();          // 내 ID
            this.selfName = u.getUserName();  // 내 닉네임

            ApiClient.HttpResult res = ApiClient.get("/chat/rooms/" + userId);
            if (!res.isOk() || res.body == null || res.body.isEmpty()) {
                roomListModel.clear();
                return;
            }

            JSONArray arr = new JSONArray(res.body);
            roomListModel.clear();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject room = arr.getJSONObject(i);
                String rId = room.getString("roomId");

                String pId = null;
                String pName = "(상대 없음)" ;

                if (room.has("participants")) {
                    JSONArray ps = room.getJSONArray("participants");
                    for (int j = 0; j < ps.length(); j++) {
                        JSONObject p = ps.getJSONObject(j);
                        String uid = p.optString("userId", "");
                        String uname = p.optString("userName", "");

                        if (!uid.isEmpty() && !uid.equals(userId)) {
                            pId = uid;
                            pName = !uname.isEmpty() ? uname : uid;
                            break;
                        }
                    }
                }

                String time = room.optString("lastMessageAt", "");

                RoomItem item = new RoomItem(rId, pId, pName, time);
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

        // 상단 영역
        JPanel topBox = new JPanel(new BorderLayout());
        topBox.setBackground(colorTop);
        topBox.setPreferredSize(new Dimension(200, 70));
        topBox.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JLabel avatar = new JLabel(new ImageIcon("images/default_profile.png"));
        avatar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topBox.add(avatar, BorderLayout.WEST);

        topNameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        topBox.add(topNameLabel, BorderLayout.CENTER);

        JButton homeButton = new JButton("홈으로") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(colorOther.darker());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };

        homeButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        homeButton.setForeground(new Color(80, 80, 80));
        homeButton.setBackground(Color.WHITE);
        homeButton.setOpaque(false);
        homeButton.setFocusPainted(false);
        homeButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

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
        bottomBox.setBackground(Color.WHITE);
        bottomBox.setLayout(new BoxLayout(bottomBox, BoxLayout.X_AXIS));
        bottomBox.setPreferredSize(new Dimension(0, 70));
        bottomBox.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        inputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        inputField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        inputField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        bottomBox.add(inputField);
        bottomBox.add(Box.createRigidArea(new Dimension(10, 0)));

        sendButton.setIcon(new ImageIcon("images/submit.png"));
        sendButton.setPreferredSize(new Dimension(50, 50));
        sendButton.setFocusPainted(false);
        sendButton.setBackground(colorMy);
        sendButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        bottomBox.add(sendButton);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
        rightPanel.add(bottomBox, BorderLayout.SOUTH);

        return rightPanel;
    }

    // ============================ 채팅 시작 ============================

    public void startChat(
            String roomId,
            String selfId,
            String selfName,
            String partnerId,
            String partnerName
    ) {
        this.roomId = roomId;
        this.userId = selfId;
        this.selfName = selfName;
        this.partnerId = partnerId;
        this.partnerName = partnerName;

        try {
            messageArea.removeAll();
            refreshMessages();
            loadChatHistory();

            String encodedUserId = URLEncoder.encode(selfId, StandardCharsets.UTF_8.toString());
            String encodedUserName = URLEncoder.encode(selfName, StandardCharsets.UTF_8.toString());

            String wsUrl = "ws://localhost:8080/ws/chat/"
                    + roomId + "/"
                    + encodedUserId + "/"
                    + encodedUserName;


            socketClient = new WebSocketClientChat(wsUrl);
            socketClient.onJson(json -> SwingUtilities.invokeLater(() -> receiveJson(json)));
            socketClient.connect();


            if (partnerName != null && !partnerName.isEmpty()) {
                topNameLabel.setText("상대: " + partnerName);
            } else {
                topNameLabel.setText("채팅방: " + roomId.substring(0, Math.min(roomId.length(), 6)) + "...");
            }

            addSystemMessage("채팅방에 입장했습니다.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================ 이전 채팅 내역 불러오기 ============================

    private void loadChatHistory() {
        try {
            ApiClient.HttpResult result = ApiClient.get("/chat/" + roomId);
            if (result == null || result.body == null || result.body.isEmpty()) return;

            JSONObject root = new JSONObject(result.body);
            if (!root.has("chatHistory")) return;

            JSONArray arr = root.getJSONArray("chatHistory");

            for (int i = 0; i < arr.length(); i++) {
                JSONObject m = arr.getJSONObject(i);
                String senderId = m.optString("senderId", "");
                String senderName = m.optString("senderName", senderId);
                String text = m.optString("message", "");

                if (senderId.equals(userId)) {
                    addMyMessage(text);
                } else {
                    addOtherMessage(senderName + ": " + text);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================ 메시지 전송/수신 ============================

    private boolean sending = false;

    private synchronized void sendMessage() {
        if (sending) return;
        sending = true;

        String msg = inputField.getText().trim();
        if (!msg.isEmpty() && socketClient != null) {

            try {
                JSONObject json = new JSONObject();
                json.put("type", "CHAT");

                JSONObject data = new JSONObject();
                data.put("senderId", userId);     // 로그인 ID
                data.put("senderName", selfName); // 닉네임
                data.put("content", msg);

                json.put("data", data);

                socketClient.sendChat(userId, selfName, msg);

                addMyMessage(msg);
                inputField.setText("");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        sending = false;
    }

    // 입퇴장 알림 있는 버전
    //    private void receiveJson(JSONObject json) {
//        String type = json.optString("type");
//
//        switch (type) {
//            case "JOIN": {
//                JSONObject data = json.getJSONObject("data");
//                String joinedName = data.optString("userName", "알 수 없음");
//                addSystemMessage("🔔 " + joinedName + " 님이 입장했습니다.");
//                break;
//            }
//
//            case "CHAT": {
//                JSONObject data = json.getJSONObject("data");
//                String senderId = data.optString("senderId");
//                String senderName = data.optString("senderName");
//                String content = data.optString("content");
//
//                if (senderId.equals(userId))
//                    return;  // 내가 보낸 메시지는 무시
//
//                addOtherMessage(senderName + ": " + content);
//                break;
//            }
//
//            case "LEAVE": {
//                JSONObject data = json.getJSONObject("data");
//                addSystemMessage("❌ " + data.optString("userName") + " 님이 퇴장했습니다.");
//                break;
//            }
//
//            default:
//                addSystemMessage("알 수 없는 메시지: " + json);
//        }
//    }
    private void receiveJson(JSONObject json) {
        String type = json.optString("type");

        switch (type) {

            case "JOIN": {
                JSONObject data = json.getJSONObject("data");
                String joinedName = data.optString("userName", "알 수 없음");

                // 🔥 UI 출력 X → 로그만
                System.out.println("[CHAT] JOIN: " + joinedName);
                break;
            }

            case "CHAT": {
                JSONObject data = json.getJSONObject("data");
                String senderId = data.optString("senderId");
                String senderName = data.optString("senderName");
                String content = data.optString("content");

                // 🔥 내가 보낸 메시지는 무시
                if (senderId.equals(userId)) return;

                addOtherMessage(senderName + ": " + content);
                break;
            }

            case "LEAVE": {
                JSONObject data = json.getJSONObject("data");
                String leftName = data.optString("userName", "알 수 없음");

                // 🔥 UI 출력 X → 로그만
                System.out.println("[CHAT] LEAVE: " + leftName);
                break;
            }

            default:
                System.out.println("[CHAT] Unknown Type: " + json);
        }
    }


    // ============================ 메시지 UI ============================

    private void addMyMessage(String msg) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);

        BubbleLabel label = new BubbleLabel(msg, colorMy);

        panel.add(label);
        messageArea.add(panel);
        messageArea.add(Box.createVerticalStrut(8));
        refreshMessages();
    }

    private void addOtherMessage(String msg) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);

        BubbleLabel label = new BubbleLabel(msg, colorOther);

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
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = ((JScrollPane) messageArea.getParent().getParent()).getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    public void closeChat() {
        if (socketClient != null) {
            socketClient.close();
            addSystemMessage("채팅방을 나갔습니다.");
        }
    }
}
