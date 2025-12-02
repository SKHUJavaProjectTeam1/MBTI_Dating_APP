package com.mbtidating.controller;

import com.mbtidating.dto.ChatRoom;
import com.mbtidating.dto.ChatRoom.Message;
import com.mbtidating.dto.CreateRoomRequest;
import com.mbtidating.service.ChatRoomService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

	private final ChatRoomService chatRoomService;

	/** 채팅방 생성 또는 기존 방 반환 */
	@PostMapping
	public ChatRoom createRoom(@RequestBody CreateRoomRequest req) {
		return chatRoomService.createOrGetRoom(req);
	}

	/** 유저의 채팅방 목록 조회 */
	@GetMapping("/user/{userId}")
	public List<ChatRoom> getRoomsByUser(@PathVariable String userId) {
		return chatRoomService.getRoomsByUser(userId);
	}

	/** 특정 채팅방 정보 조회 */
	@GetMapping("/{roomId}")
	public ChatRoom getRoom(@PathVariable String roomId) {
		return chatRoomService.getRoom(roomId);
	}

	@DeleteMapping("/{roomId}")
	public ResponseEntity<?> deleteRoom(@PathVariable String roomId) {

		ChatRoom room = chatRoomService.getRoom(roomId);
		if (room == null) {
			return ResponseEntity.status(404).body("Room not found");
		}

		chatRoomService.deleteRoom(roomId);
		return ResponseEntity.ok("OK");
	}

}
