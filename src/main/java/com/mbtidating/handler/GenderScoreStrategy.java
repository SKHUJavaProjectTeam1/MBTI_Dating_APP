package com.mbtidating.handler;

import com.mbtidating.dto.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenderScoreStrategy implements MatchStrategy {

    @Override
    public int score(User me, User target) {

        String g1 = me.getGender();
        String g2 = target.getGender();

        // 성별 정보가 없으면 0점
        if (g1 == null || g2 == null) {
            log.info("  [GenderScore] {}(g1=null) → {}(g2=null): 0점",
                    me.getUserName(), target.getUserName());
            return 0;
        }

        g1 = g1.trim().toUpperCase();
        g2 = g2.trim().toUpperCase();

        int score = 0;

        // 둘 중 하나라도 "O" → 가벼운 가산점
        if (g1.equals("O") || g2.equals("O")) {
            score = 2;
        }
        // F ↔ M → 가장 높은 점수
        else if (!g1.equals(g2)) {
            score = 5;
        }
        // 같은 성별 → 낮은 점수
        else {
            score = 1;
        }

        // 로그 출력 (MbtiScoreStrategy 동일 포맷)
        log.info("  [GenderScore] {}({}) → {}({}): {}점",
                me.getUserName(), g1,
                target.getUserName(), g2,
                score
        );

        return score;
    }
}
