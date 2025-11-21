package com.mbtidating.handler;

import com.mbtidating.dto.User;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CompositeMatchStrategy {

    private final List<MatchStrategy> strategies = new ArrayList<>();

    public CompositeMatchStrategy add(MatchStrategy s) {
        strategies.add(s);
        return this;
    }

    public User findMatch(User me, List<User> candidates) {
        User best = null;
        int bestScore = Integer.MIN_VALUE;

        for (User target : candidates) {
            if (target.getUserName().equals(me.getUserName())) continue;

            int total = 0;

            for (MatchStrategy s : strategies) {
                int sc = s.score(me, target);
                total += sc;

                log.info("[Score] {} → {}: {}점 (전략={})",
                        me.getUserName(), target.getUserName(), sc,
                        s.getClass().getSimpleName());
            }

            log.info("[Total Score] {} → {}: {}점",
                    me.getUserName(), target.getUserName(), total);

            if (total > bestScore) {
                bestScore = total;
                best = target;
            }
        }

        log.info("🎯 최종 선택: {} (총점: {})", best != null ? best.getUserName() : "없음", bestScore);
        return best;
    }
}
