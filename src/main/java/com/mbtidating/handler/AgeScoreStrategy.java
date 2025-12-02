package com.mbtidating.handler;

import com.mbtidating.dto.User;

public class AgeScoreStrategy implements MatchStrategy {

    @Override
    public int score(User me, User target) {

        if (me.getAge() == null || target.getAge() == null)
            return 0;

        int diff = Math.abs(me.getAge() - target.getAge());

        if (diff <= 2) return 3;      // 매우 가까운 나이
        if (diff <= 5) return 2;
        if (diff <= 8) return 1;
        return 0;
    }
}
