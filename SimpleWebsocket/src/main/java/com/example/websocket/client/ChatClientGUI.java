package com.example.websocket.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.*;
import java.awt.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

// 메시지 DTO
class ChatMessage {
    private String sender;
    private String content;

    public ChatMessage() {} // Jackson용

    public ChatMessage(String sender, String content) {
        this.sender = sender;
        this.content = content;
    }

    public String getSender() { return sender; }
    public String getContent() { return content; }

    public void setSender(String sender) { this.sender = sender; }
    public void setContent(String content) { this.content = content; }
}

public class ChatClientGUI extends JFrame {

    private JTextArea textArea;
    private JTextField inputField;
    private WebSocketClient client;
    private String nickname;
    private ObjectMapper mapper = new ObjectMapper();

    public ChatClientGUI(String nickname) {
        this.nickname = nickname;

        setTitle("WebSocket Chat - " + nickname);
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        textArea = new JTextArea();
        textArea.setEditable(false);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        inputField = new JTextField();
        add(inputField, BorderLayout.SOUTH);

        inputField.addActionListener(e -> {
            String msg = inputField.getText();
            if (!msg.isEmpty() && client != null) {
                sendMessage(msg);
                inputField.setText("");
            }
        });

        connectWebSocket();
        setVisible(true);
    }

    private void connectWebSocket() {
        try {
            client = new WebSocketClient(new URI("ws://localhost:8080/chat")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    appendMessage("서버 연결됨!");
                }

                @Override
                public void onMessage(String message) {
                    try {
                        ChatMessage msg = mapper.readValue(message, ChatMessage.class);
                        appendMessage(msg.getSender() + ": " + msg.getContent());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    appendMessage("연결 종료: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
            client.connectBlocking();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String msg) {
        try {
            ChatMessage message = new ChatMessage(nickname, msg);
            String json = mapper.writeValueAsString(message);
            client.send(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void appendMessage(String msg) {
        SwingUtilities.invokeLater(() -> textArea.append(msg + "\n"));
    }

    public static void main(String[] args) {
        String name1 = JOptionPane.showInputDialog("닉네임 입력 (1번째 GUI)");
        String name2 = JOptionPane.showInputDialog("닉네임 입력 (2번째 GUI)");

        SwingUtilities.invokeLater(() -> {
            new ChatClientGUI(name1);
            new ChatClientGUI(name2);
        });
    }
}
