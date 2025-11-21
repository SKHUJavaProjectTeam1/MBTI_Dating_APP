package com.mbtidating.view;

import com.mbtidating.network.WebSocketClient;
import javax.swing.*;
import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ChatView extends JPanel {

    private final MainApp mainApp;
    private WebSocketClient socketClient;
    private String roomId;
    private String userName;

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
        leftPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 20));

        JLabel titleLabel = new JLabel("채팅방");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        leftPanel.add(titleLabel);
        return leftPanel;
    }

    private JPanel buildRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

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

        messageArea.setBackground(new Color(255, 240, 240));
        messageArea.setLayout(new BoxLayout(messageArea, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(messageArea);
        scroll.setBorder(null);
        rightPanel.add(scroll, BorderLayout.CENTER);

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

    // ✅ 수정된 부분: URL 인코딩 + 파라미터 반영
    public void startChat(String roomId, String userName) {
        this.roomId = roomId;
        this.userName = userName;

        try {
            String encodedUser = URLEncoder.encode(userName, StandardCharsets.UTF_8.toString());
            String wsUrl = "ws://localhost:8080/ws/chat/" + roomId + "/" + encodedUser;
            System.out.println("[DEBUG] Chat WebSocket URL = " + wsUrl);

            topNameLabel.setText("채팅방: " + roomId.substring(0, 6) + "...");
            socketClient = new WebSocketClient(wsUrl, userName);
            socketClient.onMessage(msg -> SwingUtilities.invokeLater(() -> receiveMessage(msg)));
            socketClient.connect();

            addSystemMessage("채팅방에 입장했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean sending = false;
    private synchronized void sendMessage() {
        if (sending) return; // 이미 전송 중이면 무시
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
        if (msg.startsWith("🔔") || msg.startsWith("❌")) {
            addSystemMessage(msg);
        } else if (msg.contains(": ")) {
            addOtherMessage(msg);
        }
    }

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

    public void closeChat() {
        if (socketClient != null) {
            socketClient.close();
            addSystemMessage("채팅방을 나갔습니다.");
        }
    }
}
