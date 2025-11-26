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

    public int calculateScore(User me, User target) {
        int total = 0;

        for (MatchStrategy s : strategies) {
            int sc = s.score(me, target);
            total += sc;
        }

        log.info("  [TotalScore] {} → {} = {}점",
                me.getUserName(), target.getUserName(), total);

        return total;
    }


    public User findMatch(User me, List<User> candidates) {
        User best = null;
        int bestScore = Integer.MIN_VALUE;

        for (User target : candidates) {
            if (target.getUserName().equals(me.getUserName())) continue;

            int total = calculateScore(me, target);

            log.info("  [CandidateScore] {} → {}: {}점",
                    me.getUserName(), target.getUserName(), total);

            if (total > bestScore) {
                bestScore = total;
                best = target;
            }
        }

        if (best != null) {
            log.info("  🎯 [SelectMatch] {} → {} (총점: {}점)",
                    me.getUserName(), best.getUserName(), bestScore);
        }

        return best;
    }

}
