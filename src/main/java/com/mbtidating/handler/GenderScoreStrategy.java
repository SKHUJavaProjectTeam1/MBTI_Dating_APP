package com.mbtidating.handler;

import com.mbtidating.dto.User;

public class GenderScoreStrategy implements MatchStrategy {

    @Override
    public int score(User me, User target) {

        String g1 = me.getGender();
        String g2 = target.getGender();

        if (g1 == null || g2 == null) return 0;

        // 둘 중 하나라도 "O" → 가벼운 가산점
        if (g1.equalsIgnoreCase("o") || g2.equalsIgnoreCase("o")) {
            return 2;
        }

        // F ↔ M → 가장 높은 점수
        if (!g1.equalsIgnoreCase(g2)) {
            return 5;
        }

        // 같은 성별 → 일단 낮은 점수
        return 1;
    }
}
