package com.mbtidating.service;

import com.mbtidating.dto.ChatRoom;
import com.mbtidating.dto.ChatRoom.Message;
import com.mbtidating.dto.ChatRoom.Participant;
import com.mbtidating.dto.CreateRoomRequest;
import com.mbtidating.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    /** 채팅방 생성 또는 기존 동일한 방 반환 */
    public ChatRoom createOrGetRoom(CreateRoomRequest req) {

        // 1) user1이 속한 모든 방 조회
        List<ChatRoom> rooms = chatRoomRepository.findByParticipantsUserId(req.getUser1());

        for (ChatRoom room : rooms) {
            boolean hasUser2 = room.getParticipants().stream()
                    .anyMatch(p -> req.getUser2().equals(p.getUserId()));
            if (hasUser2) {
                return room;    // 기존 방
            }
        }

        // 2) 방이 없으면 새 채팅방 생성
        ChatRoom newRoom = new ChatRoom();

        newRoom.getParticipants().add(new Participant(req.getUser1(), req.getUser1Name()));
        newRoom.getParticipants().add(new Participant(req.getUser2(), req.getUser2Name()));

        newRoom.setCreatedAt(Instant.now());
        newRoom.setLastMessageAt(Instant.now());

        return chatRoomRepository.save(newRoom);
    }

    /** 유저가 속한 모든 채팅방 조회 */
    public List<ChatRoom> getRoomsByUser(String userId) {
        List<ChatRoom> rooms = chatRoomRepository.findByParticipantsUserId(userId);

        // 각 방의 최근 메시지 기준으로 lastMessageAt 갱신
        for (ChatRoom room : rooms) {
            List<Message> history = room.getChatHistory();
            if (history != null && !history.isEmpty()) {
                Instant last = history.stream()
                        .map(Message::getSentAt)
                        .max(Instant::compareTo)
                        .orElse(null);
                room.setLastMessageAt(last);
            }
        }

        return rooms;
    }

    /** 메시지 전송 */
    public ChatRoom sendMessage(String roomId, Message msg) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);
        if (room == null) return null;

        msg.setSentAt(Instant.now());
        room.getChatHistory().add(msg);
        room.setLastMessageAt(msg.getSentAt());

        return chatRoomRepository.save(room);
    }
    
    public void deleteRoom(String roomId) {
        chatRoomRepository.deleteById(roomId);
    }


    /** 특정 방 조회 */
    public ChatRoom getRoom(String roomId) {
        return chatRoomRepository.findById(roomId).orElse(null);
    }
}
