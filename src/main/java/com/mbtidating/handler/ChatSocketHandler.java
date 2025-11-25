package com.mbtidating.handler;

import com.mbtidating.repository.ChatRoomRepository;
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

    @Autowired
    public void setChatRoomRepository(ChatRoomRepository repo) {
        ChatSocketHandler.roomRepo = repo;
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

    private ChatRoom getOrCreateRoom(String roomId) {
        return roomRepo.findById(roomId).orElseGet(() -> {
            ChatRoom room = new ChatRoom();
            room.setRoomId(roomId);
            return roomRepo.save(room);
        });
    }

    private void saveMessage(String roomId, String sender, String message) {
        ChatRoom room = getOrCreateRoom(roomId);
        room.getChatHistory().add(new ChatRoom.Message(sender, message));
        roomRepo.save(room);
    }


    // --- 참여자 등록 ---
    private void addParticipant(String roomId, String user) {
        ChatRoom room = getOrCreateRoom(roomId);

        boolean exists = room.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(user));

        if (!exists) {
            room.getParticipants().add(new ChatRoom.Participant(user));
            roomRepo.save(room);
        }
    }


    // --- WebSocket Events ---
    @OnOpen
    public void onOpen(Session session,
                       @PathParam("roomId") String roomId,
                       @PathParam("user") String user) throws IOException {

        rooms.putIfAbsent(roomId, new ConcurrentHashMap<>());
        rooms.get(roomId).put(user, session);

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

