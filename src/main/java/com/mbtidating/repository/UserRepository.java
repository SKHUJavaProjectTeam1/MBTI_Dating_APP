package com.mbtidating.repository;

import com.mbtidating.dto.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
	Optional<User> findByUserName(String userName); // 로그인용
}
