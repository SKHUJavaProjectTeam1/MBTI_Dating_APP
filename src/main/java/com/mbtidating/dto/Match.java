package com.mbtidating.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "messages")
public class Match {

    @Id
    private String id;
    private String matchId;
    private Instant matchedAt = Instant.now();
    private List<Participant> participants = new ArrayList<>();
    private List<ChatMessage> chatHistory = new ArrayList<>();

    // ✅ getter/setter for matchedAt
    public Instant getMatchedAt() {
        return matchedAt;
    }

    public void setMatchedAt(Instant matchedAt) {
        this.matchedAt = matchedAt;
    }

    // ✅ getter/setter for matchId
    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    // ✅ getter/setter for participants & chatHistory
    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public List<ChatMessage> getChatHistory() {
        return chatHistory;
    }

    public void setChatHistory(List<ChatMessage> chatHistory) {
        this.chatHistory = chatHistory;
    }

    // === 내부 클래스들 ===
    public static class Participant {
        private String userId;

        public Participant() {}
        public Participant(String userId) { this.userId = userId; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }

    public static class ChatMessage {
        private String senderId;
        private String message;
        private Instant sentAt;
        private boolean isRead;

        public ChatMessage() {}
        public ChatMessage(String senderId, String message) {
            this.senderId = senderId;
            this.message = message;
            this.sentAt = Instant.now();
            this.isRead = false;
        }

        public String getSenderId() { return senderId; }
        public void setSenderId(String senderId) { this.senderId = senderId; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Instant getSentAt() { return sentAt; }
        public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

        public boolean isRead() { return isRead; }
        public void setRead(boolean read) { isRead = read; }
    }
}
