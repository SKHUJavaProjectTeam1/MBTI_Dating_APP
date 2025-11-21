package com.mbtidating.handler;

import com.mbtidating.dto.User;

public interface MatchStrategy {
    int score(User me, User target);
}
