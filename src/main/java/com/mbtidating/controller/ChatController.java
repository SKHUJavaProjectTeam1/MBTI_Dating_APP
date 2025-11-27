package com.mbtidating.controller;

import com.mbtidating.dto.ChatRoom;
import com.mbtidating.repository.ChatRoomRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;   // ★★★ 반드시 추가해야 함
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatRoomRepository chatRoomRepository;

    public ChatController(ChatRoomRepository chatRoomRepository) {
        this.chatRoomRepository = chatRoomRepository;
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
