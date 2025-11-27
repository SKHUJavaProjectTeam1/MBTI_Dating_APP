package com.mbtidating.handler;

import com.mbtidating.repository.ChatRoomRepository;
import com.mbtidating.repository.UserRepository;
import com.mbtidating.dto.User;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.mbtidating.dto.ChatRoom;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/chat/{roomId}/{user}")
public class ChatSocketHandler {

    private static ChatRoomRepository roomRepo;
    private static UserRepository userRepo;

    @Autowired
    public void setChatRoomRepository(ChatRoomRepository repo) {
        ChatSocketHandler.roomRepo = repo;
    }
    
    @Autowired
    public void setUserRepository(UserRepository repo) {
        ChatSocketHandler.userRepo = repo;
    }

    private static final Map<String, Map<String, Session>> rooms = new ConcurrentHashMap<>();


    // --- 공통 유틸 ---
    private void safeSend(Session s, String msg) throws IOException {
        synchronized (s) {
            if (s.isOpen()) s.getBasicRemote().sendText(msg);
        }
    }

    private void broadcast(String roomId, String msg) {
        var room = rooms.get(roomId);
        if (room == null) return;

        for (Session s : room.values()) {
            try {
                safeSend(s, msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ChatRoom getRoomOrNull(String roomId) {
        return roomRepo.findById(roomId).orElse(null);
    }


    private void saveMessage(String roomId, String senderId, String message) {
    	ChatRoom room = roomRepo.findById(roomId).orElse(null);
    	if (room == null) {
            System.out.println("[WARN] Message save failed: room does not exist → " + roomId);
            return;
        }
        String senderName = room.getParticipants().stream()
                .filter(p -> p.getUserId().equals(senderId))
                .map(ChatRoom.Participant::getUserName)
                .findFirst()
                .orElse(senderId);
        
        room.getChatHistory().add(
                new ChatRoom.Message(senderId, senderName, message)
        );
        
        roomRepo.save(room);
    }


    // --- 참여자 등록 ---
    private void addParticipant(String roomId, String userId) {
    	ChatRoom room = roomRepo.findById(roomId).orElse(null);
    	if (room == null) {
    	    System.out.println("[WARN] addParticipant failed: room does not exist → " + roomId);
    	    return;
    	}

        boolean exists = room.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(userId));

        if (!exists) {

            // 🔥 DB에서 사용자 조회
            User u = userRepo.findById(userId).orElse(null);
            String username = (u != null ? u.getUserName() : userId);

            room.getParticipants().add(
                new ChatRoom.Participant(userId, username)
            );

            roomRepo.save(room);
        }
    }


    // --- WebSocket Events ---
    @OnOpen
    public void onOpen(Session session,
                       @PathParam("roomId") String roomId,
                       @PathParam("user") String user) throws IOException {
    	 Map<String, Session> room = rooms.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());
    	
    	 Session old = room.get(user);
    	    if (old != null && old.isOpen()) {
    	        try { old.close(); } catch (Exception ignored) {}
    	    }

    	    room.put(user, session);

    	    addParticipant(roomId, user);

        broadcast(roomId, "🔔 " + user + " 님이 입장했습니다.");
        System.out.println("[CHAT] 입장: " + roomId + " / " + user);
    }

    @OnMessage
    public void onMessage(String msg,
                          @PathParam("roomId") String roomId,
                          @PathParam("user") String user) throws IOException {

        saveMessage(roomId, user, msg);

        String fullMsg = user + ": " + msg;
        broadcast(roomId, fullMsg);
    }

    @OnClose
    public void onClose(Session session,
                        @PathParam("roomId") String roomId,
                        @PathParam("user") String user) {

        var room = rooms.get(roomId);
        if (room != null) {
            room.remove(user);
        }

        new Thread(() -> {
            try {
                Thread.sleep(50);
                broadcast(roomId, "❌ " + user + " 님이 퇴장했습니다.");
            } catch (Exception ignored) {}
        }).start();

        System.out.println("[CHAT] 퇴장: " + user);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        t.printStackTrace();
    }
}

