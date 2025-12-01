package com.mbtidating.controller;

import com.mbtidating.dto.ChatRoom;
import com.mbtidating.repository.ChatRoomRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;   // ★★★ 반드시 추가해야 함
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatRoomRepository chatRoomRepository;

    public ChatController(ChatRoomRepository chatRoomRepository) {
        this.chatRoomRepository = chatRoomRepository;
    }
    
    @PostMapping("/createRoom")
    public ChatRoom createOrGetRoom(@RequestBody Map<String, String> body) {

        String user1 = body.get("user1");
        String user2 = body.get("user2");

        if (user1 == null || user2 == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user1, user2 필요");
        }

        // 1) 두 사람이 참여한 방이 이미 있는지 확인
        List<ChatRoom> rooms = chatRoomRepository.findByParticipantsUserId(user1);
        for (ChatRoom r : rooms) {
            boolean hasUser2 = r.getParticipants().stream()
                    .anyMatch(p -> user2.equals(p.getUserId()));
            if (hasUser2) {
                return r;  // 기존 방 반환
            }
        }

        // 2) 없으면 새 방 생성
        ChatRoom newRoom = new ChatRoom();

        newRoom.getParticipants().add(new ChatRoom.Participant(user1, body.get("user1Name")));
        newRoom.getParticipants().add(new ChatRoom.Participant(user2, body.get("user2Name")));

        newRoom.setCreatedAt(Instant.now());
        newRoom.setLastMessageAt(Instant.now());

        return chatRoomRepository.save(newRoom);
    }


    @GetMapping("/{roomId}")
    public ChatRoom getRoom(@PathVariable String roomId) {
        return chatRoomRepository.findById(roomId).orElse(null);
    }

    @GetMapping("/rooms/{userId}")
    public List<ChatRoom> getMyChatRooms(@PathVariable String userId) {
        List<ChatRoom> rooms = chatRoomRepository.findByParticipantsUserId(userId);

        for (ChatRoom room : rooms) {
            List<ChatRoom.Message> history = room.getChatHistory();
            if (history != null && !history.isEmpty()) {

                // 최신 메시지 시간 찾기
                Instant lastTime = history.stream()
                        .map(ChatRoom.Message::getSentAt)
                        .filter(sentAt -> sentAt != null)
                        .max(Instant::compareTo)
                        .orElse(null);

                room.setLastMessageAt(lastTime);
            }
        }

        return rooms;
    }
}
