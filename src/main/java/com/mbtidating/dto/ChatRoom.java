package com.mbtidating.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "chatrooms")
public class ChatRoom {

    @Id
    private String roomId;

    private Instant createdAt = Instant.now();
    private Instant lastMessageAt;

    private List<Participant> participants = new ArrayList<>();
    private List<Message> chatHistory = new ArrayList<>();

    // ===== Getter/Setter =====

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(Instant lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
    public List<Participant> getParticipants() { return participants; }
    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public List<Message> getChatHistory() { return chatHistory; }
    public void setChatHistory(List<Message> chatHistory) {
        this.chatHistory = chatHistory;
    }

    // ===== 내부 클래스 =====

    public static class Participant {
        private String userId;

        public Participant() {}
        public Participant(String userId) { this.userId = userId; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }

    public static class Message {
        private String senderId;
        private String message;
        private Instant sentAt;
        private boolean isRead;
        private Instant lastMessageAt;

        public Message() {}
        public Message(String senderId, String message) {
            this.senderId = senderId;
            this.message = message;
            this.sentAt = Instant.now();
            this.isRead = false;
        }

        public String getSenderId() { return senderId; }
        public void setSenderId(String senderId) { this.senderId = senderId; }

 

        public Instant getLastMessageAt() { return lastMessageAt; }
        public void setLastMessageAt(Instant lastMessageAt) { this.lastMessageAt = lastMessageAt; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Instant getSentAt() { return sentAt; }
        public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

        public boolean isRead() { return isRead; }
        public void setRead(boolean read) { isRead = read; }
    }
}
