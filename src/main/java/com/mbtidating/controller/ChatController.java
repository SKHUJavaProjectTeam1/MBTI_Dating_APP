package com.mbtidating.controller;

import com.mbtidating.dto.ChatRoom;
import com.mbtidating.repository.ChatRoomRepository;
import org.springframework.web.bind.annotation.*;

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
        return chatRoomRepository.findByParticipantsUserId(userId);
    }
}
