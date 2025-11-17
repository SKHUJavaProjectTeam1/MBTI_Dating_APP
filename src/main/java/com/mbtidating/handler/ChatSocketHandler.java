package com.mbtidating.handler;

import com.mbtidating.dto.Match;
import com.mbtidating.repository.MatchRepository;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/chat/{roomId}/{user}")
public class ChatSocketHandler {

    private static MatchRepository staticRepo;
    @Autowired
    public void setRepo(MatchRepository repo) {
        ChatSocketHandler.staticRepo = repo;
    }

    private static final Map<String, Map<String, Session>> rooms = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("roomId") String roomId,
                       @PathParam("user") String user) {

        rooms.putIfAbsent(roomId, new ConcurrentHashMap<>());
        Map<String, Session> room = rooms.get(roomId);

        // 기존 세션 닫기
        Session old = room.get(user);
        if (old != null && old != session) {
            // close() 하지 않고 그냥 새 세션으로 덮어쓰기만 함
            room.put(user, session);
        } else {
            room.put(user, session);
        }
        
        System.out.println("💬 연결됨 [" + roomId + "] " + user);

        // ✅ DB에서 이전 대화 불러오기
        staticRepo.findByMatchId(roomId).ifPresentOrElse(
                match -> sendHistory(session, match),
                () -> createMatchRecord(roomId, user)
        );

        broadcast(roomId, "🔔 " + user + " 님이 입장했습니다.", user);
    }

    @OnMessage
    public void onMessage(String msg,
                          @PathParam("roomId") String roomId,
                          @PathParam("user") String user) {

        if (msg.trim().startsWith("{") && msg.contains("\"type\":\"enqueue\"")) {
            // 무시 (로그만 남김)
            System.out.println("⚙️ [" + roomId + "] " + user + ": enqueue 메시지 무시");
            return;
        }

        System.out.println("📩 [" + roomId + "] " + user + ": " + msg);

        Match match = staticRepo.findByMatchId(roomId)
                .orElseGet(() -> createMatchRecord(roomId, user));

        Match.ChatMessage chatMsg = new Match.ChatMessage(user, msg);
        match.getChatHistory().add(chatMsg);
        staticRepo.save(match);

        broadcast(roomId, user + ": " + msg, user);
    }


    @OnClose
    public void onClose(Session session,
                        @PathParam("roomId") String roomId,
                        @PathParam("user") String user) {

        Map<String, Session> room = rooms.get(roomId);
        if (room != null) room.remove(user);

        broadcast(roomId, "❌ " + user + " 님이 퇴장했습니다.", user);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("⚠️ 채팅 오류: " + throwable.getMessage());
    }

    private void broadcast(String roomId, String msg, String sender) {
        Map<String, Session> room = rooms.get(roomId);
        if (room == null) return;

        Iterator<Map.Entry<String, Session>> it = room.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Session> entry = it.next();
            String user = entry.getKey();
            Session s = entry.getValue();
            try {
                if (s.isOpen() && !user.equals(sender)) {
                    synchronized (s) { s.getBasicRemote().sendText(msg); }
                } else if (!s.isOpen()) {
                    it.remove();
                }
            } catch (IOException e) {
                it.remove();
            }
        }
    }

    private void sendHistory(Session session, Match match) {
        try {
            session.getBasicRemote().sendText("📜 [이전 대화 기록]");
            for (Match.ChatMessage msg : match.getChatHistory()) {
                session.getBasicRemote().sendText(
                        msg.getSenderId() + ": " + msg.getMessage()
                );
            }
            session.getBasicRemote().sendText("📜 [대화 기록 끝]");
        } catch (IOException e) {
            System.err.println("⚠️ 히스토리 전송 오류: " + e.getMessage());
        }
    }

    private Match createMatchRecord(String roomId, String user) {
        Match m = new Match();
        m.setMatchId(roomId);
        m.getParticipants().add(new Match.Participant(user));
        m.setMatchedAt(Instant.now());
        return staticRepo.save(m);
    }
}
