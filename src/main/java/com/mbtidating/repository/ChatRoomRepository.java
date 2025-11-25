package com.mbtidating.repository;

import com.mbtidating.dto.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    List<ChatRoom> findByParticipantsUserId(String userId);
}
