package com.mbtidating.controller;

import com.mbtidating.dto.ChatRoom;
import com.mbtidating.dto.ChatRoom.Message;
import com.mbtidating.service.ChatRoomService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat/messages")
@RequiredArgsConstructor
public class MessageController {

    private final ChatRoomService chatRoomService;

    /** 메시지 전송 */
    @PostMapping
    public ChatRoom sendMessage(@RequestParam String roomId,
                                @RequestBody Message msg) {
        return chatRoomService.sendMessage(roomId, msg);
    }

    /** 특정 방의 전체 메시지 조회 */
    @GetMapping("/{roomId}")
    public ChatRoom getMessages(@PathVariable String roomId) {
        return chatRoomService.getRoom(roomId);
    }
}
