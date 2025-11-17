package com.mbtidating.handler;

import com.mbtidating.dto.User;
import java.util.*;

public class CompositeMatchStrategy implements MatchStrategy {

    private final List<MatchStrategy> strategies = new ArrayList<>();

    public CompositeMatchStrategy add(MatchStrategy s) {
        strategies.add(s);
        return this;
    }

    @Override
    public User findMatch(User me, List<User> candidates) {
        for (MatchStrategy s : strategies) {
            User match = s.findMatch(me, candidates);
            if (match != null)
                return match;
        }
        return null;
    }
}
