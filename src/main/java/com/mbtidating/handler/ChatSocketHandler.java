package com.mbtidating.handler;

import com.mbtidating.repository.ChatRoomRepository;
import com.mbtidating.dto.ChatRoom;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/chat/{roomId}/{userId}/{userName}")
public class ChatSocketHandler {

    private static ChatRoomRepository roomRepo;

    @Autowired
    public void setChatRoomRepository(ChatRoomRepository repo) {
        ChatSocketHandler.roomRepo = repo;
    }

    private static MongoTemplate mongoTemplate;

    @Autowired
    public void setMongoTemplate(MongoTemplate template) {
        ChatSocketHandler.mongoTemplate = template;
    }

    // 모든 WebSocket 연결 유지
    private static final Map<String, Map<String, Session>> rooms = new ConcurrentHashMap<>();


    // ---------- 메시지 전송 ----------
    private void safeSend(Session s, String msg) throws IOException {
        synchronized (s) {
            if (s.isOpen()) s.getBasicRemote().sendText(msg);
        }
    }

    private void broadcast(String roomId, String msg) {
        var room = rooms.get(roomId);
        if (room == null) return;

        for (Session s : room.values()) {
            try { safeSend(s, msg); }
            catch (Exception ignored) {}
        }
    }


    // ---------- DB: Room 생성 ----------
    private ChatRoom getOrCreateRoom(String roomId) {
        return roomRepo.findById(roomId).orElseGet(() -> {
            ChatRoom room = new ChatRoom();
            room.setRoomId(roomId);
            return roomRepo.save(room);
        });
    }


    // ---------- DB: Message 저장 ----------
    private void saveMessage(String roomId, String senderId, String senderName, String message) {

        Query query = new Query(Criteria.where("_id").is(roomId));

        Update update = new Update()
                .push("chatHistory").each(new ChatRoom.Message(senderId, senderName, message))
                .set("lastMessageAt", Instant.now());

        mongoTemplate.updateFirst(query, update, ChatRoom.class);
    }



    // ---------- DB: Participant 저장 (덮어쓰기 금지!) ----------
    private void addParticipant(String roomId, String userId, String userName) {

        Query query = new Query(Criteria.where("_id").is(roomId));

        // 패치 안 될 수도 있으니 upsert 사용
        Update update = new Update()
                .addToSet("participants", new ChatRoom.Participant(userId, userName));

        mongoTemplate.upsert(query, update, ChatRoom.class);

        System.out.println(">>> ADD PARTICIPANT: " + userId + " (" + userName + ")");
    }



    // ---------- WebSocket Events ----------
    @OnOpen
    public void onOpen(
            Session session,
            @PathParam("roomId") String roomId,
            @PathParam("userId") String userId,
            @PathParam("userName") String userName
    ) {

        rooms.putIfAbsent(roomId, new ConcurrentHashMap<>());
        rooms.get(roomId).put(userId, session);

        addParticipant(roomId, userId, userName);

        JSONObject msg = new JSONObject();
        msg.put("type", "JOIN");

        JSONObject data = new JSONObject();
        data.put("userId", userId);
        data.put("userName", userName);

        msg.put("data", data);

        broadcast(roomId, msg.toString());
    }


    @OnMessage
    public void onMessage(
            String message,
            @PathParam("roomId") String roomId,
            @PathParam("userId") String userId
    ) {

        JSONObject json = new JSONObject(message);
        String type = json.getString("type");

        if (type.equals("CHAT")) {

            JSONObject data = json.getJSONObject("data");
            String senderId = data.getString("senderId");
            String senderName = data.getString("senderName");
            String content = data.getString("content");

            saveMessage(roomId, senderId, senderName, content);

            JSONObject out = new JSONObject();
            out.put("type", "CHAT");

            JSONObject d = new JSONObject();
            d.put("senderId", senderId);
            d.put("senderName", senderName);
            d.put("content", content);
            d.put("sentAt", Instant.now().toString());

            out.put("data", d);

            broadcast(roomId, out.toString());
        }
    }


    @OnClose
    public void onClose(
            Session session,
            @PathParam("roomId") String roomId,
            @PathParam("userId") String userId,
            @PathParam("userName") String userName
    ) {

        var room = rooms.get(roomId);
        if (room != null) room.remove(userId);

        JSONObject out = new JSONObject();
        out.put("type", "LEAVE");

        JSONObject d = new JSONObject();
        d.put("userId", userId);
        d.put("userName", userName);

        out.put("data", d);

        broadcast(roomId, out.toString());

        System.out.println("[CHAT] 퇴장: " + userName + " (" + userId + ")");
    }


    @OnError
    public void onError(Session session, Throwable t) {
        t.printStackTrace();
    }
}
