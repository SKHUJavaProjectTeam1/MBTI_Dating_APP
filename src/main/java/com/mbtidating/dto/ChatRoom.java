package com.mbtidating.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        private String userName;

        public Participant() {}

        public Participant(String userId, String userName) {
            this.userId = userId;
            this.userName = userName;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Participant)) return false;
            Participant p = (Participant) o;
            return Objects.equals(userId, p.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId);
        }
    }



    public static class Message {
        private String senderId;     // 식별자
        private String senderName;   // 닉네임
        private String message;
        private Instant sentAt;
        private boolean isRead;

        public Message() {}

        public Message(String senderId, String senderName, String message) {
            this.senderId = senderId;
            this.senderName = senderName;
            this.message = message;
            this.sentAt = Instant.now();
            this.isRead = false;
        }

        public String getSenderId() { return senderId; }
        public void setSenderId(String senderId) { this.senderId = senderId; }

        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Instant getSentAt() { return sentAt; }
        public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

        public boolean isRead() { return isRead; }
        public void setRead(boolean read) { isRead = read; }
    }

}
