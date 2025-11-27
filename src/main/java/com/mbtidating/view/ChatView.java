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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ChatView extends JPanel {

    private final MainApp mainApp;
    private WebSocketClient socketClient;
    private String roomId;
    private String userName;

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
        roomList.setCellRenderer(new RoomListRenderer()); // ★ 커스텀 렌더러
        roomList.setOpaque(false);
        roomList.setBackground(new Color(0, 0, 0, 0));
        roomList.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

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

    // ============================ [핵심] 커스텀 렌더러 (시간 표시 추가) ============================
    private class RoomListRenderer extends JPanel implements ListCellRenderer<RoomItem> {
        private final JLabel nameLabel = new JLabel();
        private final JLabel timeLabel = new JLabel(); // ★ 시간 표시용 라벨
        private final JLabel iconLabel = new JLabel();
        private boolean isSelected = false;

        public RoomListRenderer() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setOpaque(false);

            // 1. 아이콘
            iconLabel.setPreferredSize(new Dimension(40, 40));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconLabel.setFont(new Font("Dialog", Font.BOLD, 16));
            iconLabel.setForeground(Color.WHITE);

            // 2. 이름
            nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            nameLabel.setForeground(new Color(80, 80, 80));

            // 3. 시간 (오른쪽에 작게 표시)
            timeLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            timeLabel.setForeground(new Color(150, 150, 150)); // 연한 회색
            timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            add(iconLabel, BorderLayout.WEST);
            add(nameLabel, BorderLayout.CENTER);
            add(timeLabel, BorderLayout.EAST); // ★ 오른쪽에 배치
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends RoomItem> list, RoomItem value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            this.isSelected = isSelected;

            // 이름 설정
            nameLabel.setText(value.title);

            // 아이콘 설정 (첫 글자)
            String firstLetter = (value.title != null && !value.title.isEmpty()) ? value.title.substring(0, 1) : "?";
            iconLabel.setText(firstLetter);

            // ★ 시간 설정 (날짜 변환 함수 호출)
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
    
 // ============================ [추가됨] 둥근 메시지 버블 컴포넌트 ============================

    private class BubbleLabel extends JLabel {
        private final Color bgColor;

        public BubbleLabel(String text, Color bgColor) {
            super(text);
            this.bgColor = bgColor;
            setOpaque(false); // 필수: 배경을 직접 그리기 위해 기본 투명 설정
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); // 버블 내부 여백
            setForeground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            
            // 둥근 사각형 그리기 (반지름 15)
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.dispose();
            
            super.paintComponent(g); // 텍스트를 가장 위에 그리기
        }
    }
    // ===================================================================================

    // ============================ [유틸] 날짜 변환 로직 ============================
    // 예: "2024-05-20T10:00:00" -> "방금 전" or "오전 10:00" or "어제"
    private String getRelativeTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) return "";

        try {
            // 서버가 주는 시간이 ISO-8601 형식(예: 2024-05-21T14:30:00)이라고 가정
            // 만약 서버 형식이 다르다면 DateTimeFormatter 패턴을 수정해야 함
            LocalDateTime time = LocalDateTime.parse(isoDateTime, DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime now = LocalDateTime.now();

            long diffMinutes = ChronoUnit.MINUTES.between(time, now);
            long diffDays = ChronoUnit.DAYS.between(time.toLocalDate(), now.toLocalDate());

            if (diffMinutes < 1) {
                return "방금 전";
            } else if (diffMinutes < 60) {
                return diffMinutes + "분 전";
            } else if (diffDays == 0) {
                // 오늘이면 시간만 표시 (예: 오후 3:15)
                return time.format(DateTimeFormatter.ofPattern("a h:mm"));
            } else if (diffDays == 1) {
                return "어제";
            } else {
                // 그 외에는 날짜 표시 (예: 5월 20일)
                return time.format(DateTimeFormatter.ofPattern("M월 d일"));
            }
        } catch (Exception e) {
            // 파싱 실패 시 원본 문자열 혹은 빈칸 반환
            return ""; 
        }
    }

    // ============================ 데이터 클래스 ============================
    private static class RoomItem {
        final String roomId;
        final String title;
        final String lastMessageTime; // ★ 시간 정보 필드 추가

        RoomItem(String roomId, String title, String lastMessageTime) {
            this.roomId = roomId;
            this.title = title;
            this.lastMessageTime = lastMessageTime;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    // ============================ 목록 갱신 로직 ============================
    public void refreshRoomList() {
        try {
            User u = mainApp.getLoggedInUser();
            if (u == null) return;
            this.userName = u.getId();

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
                
                // ★ 서버 JSON에서 시간 가져오기 (필드명은 서버 구현에 따라 다를 수 있음. 여기선 'lastMessageAt'으로 가정)
                // 만약 서버에서 시간을 안 준다면 빈 문자열이 됨
                String time = room.optString("lastMessageAt", ""); 

                RoomItem item = new RoomItem(rId, partner + " 님", time);
                roomListModel.addElement(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================ 오른쪽: 채팅 화면 로직 ============================

 // 기존 buildRightPanel() 메소드를 이 코드로 대체하세요.
 // 기존 buildRightPanel() 메소드를 이 코드로 대체하세요.
    private JPanel buildRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ================== [개선된 상단 박스] ==================
        JPanel topBox = new JPanel(new BorderLayout());
        topBox.setBackground(colorTop);
        topBox.setPreferredSize(new Dimension(200, 70)); // 높이를 약간 늘려 여유 공간 확보
        topBox.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15)); // 내부 여백 추가

        JLabel avatar = new JLabel(new ImageIcon("images/default_profile.png"));
        avatar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topBox.add(avatar, BorderLayout.WEST);

        // ★ 채팅방 이름 (더 크고 굵게)
        topNameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20)); 
        topBox.add(topNameLabel, BorderLayout.CENTER);

        // ★ 홈으로 버튼 (둥근 모서리 적용)
        JButton homeButton = new JButton("홈으로") {
            // 둥근 모서리 배경 그리기
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(getBackground()); // 흰색 배경 사용
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // 둥근 모서리
                g2.dispose();
                
                // 텍스트와 아이콘을 그립니다.
                super.paintComponent(g); 
            }

            // 둥근 모서리 테두리 그리기
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(colorOther.darker()); // 연한 핑크색으로 테두리 색상 지정
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        
        homeButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        homeButton.setForeground(new Color(80, 80, 80));
        homeButton.setBackground(Color.WHITE);
        homeButton.setOpaque(false); // 배경을 직접 그리기 때문에 false
        homeButton.setFocusPainted(false);
        homeButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15)); // 내부 여백
        
        homeButton.addActionListener(e -> {
            closeChat();
            mainApp.showView(MainApp.HOME);
        });
        topBox.add(homeButton, BorderLayout.EAST);

        rightPanel.add(topBox, BorderLayout.NORTH);
        // ========================================================

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

    public void startChat(String roomId, String userName) {
        this.roomId = roomId;
        this.userName = userName;

        try {
            messageArea.removeAll();
            refreshMessages();
            loadChatHistory();

            String encodedUser = URLEncoder.encode(userName, StandardCharsets.UTF_8.toString());
            String wsUrl = "ws://localhost:8080/ws/chat/" + roomId + "/" + encodedUser;

            socketClient = new WebSocketClient(wsUrl, userName);
            socketClient.onMessage(msg -> SwingUtilities.invokeLater(() -> receiveMessage(msg)));
            socketClient.connect();

            topNameLabel.setText("채팅방: " + roomId.substring(0, Math.min(roomId.length(), 6)) + "...");
            addSystemMessage("채팅방에 입장했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        try {
            data = new JSONObject(msg);
        } catch (Exception e) {
            isJson = false;
        }
        if (isJson && data != null) {
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
                    addSystemMessage("알 수 없는 메시지: " + msg);
            }
        } else {
            if (msg.startsWith("🔔") || msg.startsWith("❌")) {
                addSystemMessage(msg);
            } else if (msg.contains(": ")) {
                addOtherMessage(msg);
            }
        }
    }

 // 기존 addMyMessage() 메소드를 이 코드로 대체하세요.
    private void addMyMessage(String msg) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);

        // ★ BubbleLabel로 교체
        BubbleLabel label = new BubbleLabel(msg, colorMy); 
        // 기존에 직접 설정했던 배경, 불투명, 테두리 설정은 BubbleLabel 내부에서 처리됩니다.

        panel.add(label);
        messageArea.add(panel);
        messageArea.add(Box.createVerticalStrut(8));
        refreshMessages();
    }

 // 기존 addOtherMessage() 메소드를 이 코드로 대체하세요.
    private void addOtherMessage(String msg) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);

        // ★ BubbleLabel로 교체
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