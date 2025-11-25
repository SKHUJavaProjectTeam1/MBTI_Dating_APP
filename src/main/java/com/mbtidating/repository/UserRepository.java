package com.mbtidating.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.mbtidating.dto.User;

public interface UserRepository extends MongoRepository<User, String> {

    @Query("{ 'id' : ?0 }")
    Optional<User> findByLoginId(String id);

    Optional<User> findByUserName(String userName);
}
