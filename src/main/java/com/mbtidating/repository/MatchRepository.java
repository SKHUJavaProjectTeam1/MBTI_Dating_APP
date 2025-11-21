package com.mbtidating.repository;

import com.mbtidating.dto.Match;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface MatchRepository extends MongoRepository<Match, String> {
    Optional<Match> findByMatchId(String matchId);
}
