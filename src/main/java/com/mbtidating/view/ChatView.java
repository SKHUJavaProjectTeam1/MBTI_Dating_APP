package com.mbtidating.view;

import com.mbtidating.dto.User;
import com.mbtidating.network.ApiClient;
import com.mbtidating.network.WebSocketClientChat;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ChatView extends JPanel {

    // 🔥 상단 상대방 프로필 이미지를 넣을 라벨
    private JLabel avatarLabel;

    // ========================== 공통 예쁜 버튼 ==========================
    static class PrettyButton extends JButton {
        public PrettyButton(String text) {
            super(text);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setBackground(new Color(190, 150, 210));
            setBorder(new EmptyBorder(8, 16, 8, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg = getModel().isRollover()
                    ? new Color(210, 170, 230)
                    : new Color(190, 150, 210);

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

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
    // (지금은 PrettyButton을 쓰니까 sendButton은 안 써도 됨. 남겨두기만 함)
    private final JButton sendButton = new JButton();
    private final JLabel topNameLabel = new JLabel("상대: -", SwingConstants.LEFT);  // 상단 이름 라벨

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

        JButton deleteBtn = new JButton("선택한 방 삭제");
        deleteBtn.setBackground(new Color(255, 200, 200));
        deleteBtn.setFocusPainted(false);

        deleteBtn.addActionListener(e -> {
            RoomItem item = roomList.getSelectedValue();

            if (item == null) {
                JOptionPane.showMessageDialog(this, "삭제할 방을 선택하세요!");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "정말 삭제할까요?",
                    "채팅방 삭제",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) return;

            deleteChatRoom(item.roomId);
        });

        bottom.add(Box.createVerticalStrut(10));
        bottom.add(deleteBtn);

        return leftPanel;
    }

    private void deleteChatRoom(String roomId) {
        try {
            User me = mainApp.getLoggedInUser();
            String token = mainApp.getJwtToken();

            ApiClient.HttpResult res =
                    ApiClient.delete("/api/chat/rooms/" + roomId, token);

            if (!res.isOk()) {
                JOptionPane.showMessageDialog(this, "삭제 실패: " + res.body);
                return;
            }

            JOptionPane.showMessageDialog(this, "삭제되었습니다.");

            refreshRoomList();  // 삭제 후 목록 새로고침

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "서버 오류: " + ex.getMessage());
        }
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

            String title = (value.partnerName != null) ? value.partnerName : "(상대 없음)";
            nameLabel.setText(title);

            String firstLetter = (!title.isEmpty()) ? title.substring(0, 1) : "?";
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
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            setForeground(new Color(50, 50, 50));
            setFont(new Font("맑은 고딕", Font.PLAIN, 14));
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
        if (isoDateTime == null || isoDateTime.isEmpty()) return "";

        try {
            LocalDateTime time = LocalDateTime.parse(isoDateTime, DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime now = LocalDateTime.now();

            long diffMinutes = ChronoUnit.MINUTES.between(time, now);
            long diffDays = ChronoUnit.DAYS.between(time.toLocalDate(), now.toLocalDate());

            if (diffMinutes < 1) return "방금 전";
            else if (diffMinutes < 60) return diffMinutes + "분 전";
            else if (diffDays == 0) return time.format(DateTimeFormatter.ofPattern("a h:mm"));
            else if (diffDays == 1) return "어제";
            else return time.format(DateTimeFormatter.ofPattern("M월 d일"));

        } catch (Exception e) {
            return "";
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
            return partnerName != null ? partnerName : "(상대 없음)";
        }
    }

    // ============================ 목록 갱신 ============================

    public void refreshRoomList() {
        try {
            User u = mainApp.getLoggedInUser();
            if (u == null) return;

            this.userId = u.getId();          // 내 ID
            this.selfName = u.getUserName();  // 내 닉네임

            ApiClient.HttpResult res = ApiClient.get("/api/chat/rooms/user/" + userId);

            if (!res.isOk() || res.body == null || res.body.isEmpty()) {
                roomListModel.clear();
                return;
            }

            JSONArray arr = new JSONArray(res.body);
            roomListModel.clear();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject room = new JSONObject(arr.get(i).toString());
                String rId = room.getString("roomId");

                String pId = null;
                String pName = null;

                if (room.has("participants")) {
                    JSONArray ps = room.getJSONArray("participants");
                    for (int j = 0; j < ps.length(); j++) {
                        JSONObject p = ps.getJSONObject(j);
                        String uid = p.optString("userId", "");
                        String uname = p.optString("userName", "");

                        if (!uid.isEmpty() && !uid.equals(userId)) {
                            pId = uid;
                            if (uname != null && !uname.isEmpty()) {
                                pName = uname;
                            } else {
                                pName = uid;  // 닉네임이 없으면 userId라도 표시
                            }
                            break;
                        }
                    }
                }

                if (pName == null || pName.trim().isEmpty()) {
                    pName = "(상대 없음)";
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

        // 상단 프로필 영역
        JPanel topBox = new GradientPanel(
                new Color(255, 235, 240),   // 연핑크
                new Color(210, 255, 245)    // 민트
        );
        topBox.setLayout(new BorderLayout());
        topBox.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        topBox.setPreferredSize(new Dimension(200, 80));

        // 🔥 전역 avatarLabel 생성 + 기본 이미지
        avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(48, 48));
        avatarLabel.setIcon(new ImageIcon(
                new ImageIcon("images/default_profile.png")
                        .getImage()
                        .getScaledInstance(48, 48, Image.SCALE_SMOOTH)
        ));

        // 이름 라벨 설정
        topNameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        topNameLabel.setForeground(new Color(60, 50, 70));
        topNameLabel.setText("상대: -");

        JPanel leftProfile = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftProfile.setOpaque(false);
        leftProfile.add(avatarLabel);
        leftProfile.add(topNameLabel);

        PrettyButton homeButton = new PrettyButton("홈으로");
        homeButton.addActionListener(e -> {
            closeChat();
            mainApp.showView(MainApp.HOME);
        });

        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightBox.setOpaque(false);
        rightBox.add(homeButton);

        topBox.add(leftProfile, BorderLayout.WEST);
        topBox.add(rightBox, BorderLayout.EAST);

        rightPanel.add(topBox, BorderLayout.NORTH);

        // 메시지 영역
        messageArea.setBackground(new Color(255, 240, 240));
        messageArea.setLayout(new BoxLayout(messageArea, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(messageArea);
        scroll.setBorder(null);
        rightPanel.add(scroll, BorderLayout.CENTER);

        // 메시지 입력 영역
        JPanel bottomBox = new JPanel();
        bottomBox.setBackground(Color.WHITE);
        bottomBox.setLayout(new BoxLayout(bottomBox, BoxLayout.X_AXIS));
        bottomBox.setPreferredSize(new Dimension(0, 70));
        bottomBox.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        inputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        inputField.setBorder(BorderFactory.createLineBorder(new Color(220, 180, 200), 2, true));
        inputField.setBackground(new Color(255, 250, 252));
        inputField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        PrettyButton sendBtn = new PrettyButton("✉ 보내기");
        sendBtn.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        bottomBox.add(inputField);
        bottomBox.add(Box.createRigidArea(new Dimension(10, 0)));
        bottomBox.add(sendBtn);

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

            // 🔥 상대 프로필 이미지 로딩
            loadPartnerProfileImage();

            // 상단 이름 라벨
            String displayName = partnerName;
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = (partnerId != null && !partnerId.isEmpty())
                        ? partnerId
                        : "(상대 없음)";
            }
            topNameLabel.setText("상대: " + displayName);

            addSystemMessage("채팅방에 입장했습니다.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================ 이전 채팅 내역 불러오기 ============================

    private void loadChatHistory() {
        try {
            ApiClient.HttpResult result = ApiClient.get("/api/chat/messages/" + roomId);

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

    // 🔥 상대방 프로필 이미지 로딩
    private void loadPartnerProfileImage() {
        // partnerId 없으면 아무 것도 안 함
        if (partnerId == null || partnerId.isBlank()) return;

        try {
            String token = mainApp.getJwtToken();
            // /api/users/{id} 로 유저 정보 조회
            ApiClient.HttpResult res =
                    ApiClient.get("/api/users/" + partnerId, token);

            if (!res.isOk() || res.body == null || res.body.isBlank()) {
                return;
            }

            JSONObject obj = new JSONObject(res.body);

            // profileImg가 숫자 또는 문자열일 수 있으니 둘 다 처리
            Object imgObj = obj.opt("profileImg");
            String profileNum;

            if (imgObj instanceof Number num) {
                profileNum = String.valueOf(num.intValue());
            } else {
                profileNum = obj.optString("profileImg", "1");
            }

            // default, 빈 값 처리
            if (profileNum == null || profileNum.isBlank() || profileNum.equals("default.jpg")) {
                profileNum = "1";  // 최소한 1번은 나오게
            }

            // 클래스패스 기준 이미지 경로
            String avatarPath = "/images/profile" + profileNum + ".png";

            java.net.URL url = getClass().getResource(avatarPath);
            if (url != null) {
                Image img = new ImageIcon(url).getImage()
                        .getScaledInstance(48, 48, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(img);

                // 상단 라벨에 이미지 세팅
                avatarLabel.setIcon(icon);
            } else {
                System.out.println("⚠ 프로필 이미지 리소스를 찾을 수 없음: " + avatarPath);
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

    private void receiveJson(JSONObject json) {
        String type = json.optString("type");

        switch (type) {
            case "JOIN": {
                JSONObject data = json.getJSONObject("data");
                String joinedName = data.optString("userName", "알 수 없음");
                addSystemMessage(joinedName + " 님이 입장했습니다.");
                break;
            }

            case "CHAT": {
                JSONObject data = json.getJSONObject("data");
                String senderId = data.optString("senderId");
                String senderName = data.optString("senderName");
                String content = data.optString("content");

                if (senderId.equals(userId))
                    return;  // 내가 보낸 메시지는 무시

                addOtherMessage(senderName + ": " + content);
                break;
            }

            case "LEAVE": {
                JSONObject data = json.getJSONObject("data");
                addSystemMessage(data.optString("userName") + " 님이 퇴장했습니다.");
                break;
            }

            default:
                addSystemMessage("알 수 없는 메시지: " + json);
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

    static class GradientPanel extends JPanel {
        private final Color top, bottom;

        public GradientPanel(Color top, Color bottom) {
            this.top = top;
            this.bottom = bottom;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

}
