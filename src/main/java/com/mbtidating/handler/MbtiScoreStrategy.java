package com.mbtidating.handler;

import com.mbtidating.dto.User;
import com.mbtidating.map.MbtiScoreMap;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class MbtiScoreStrategy implements MatchStrategy {

    @Override
    public int score(User me, User target) {

        String my = convertToMbtiString(me.getMbti());
        String other = convertToMbtiString(target.getMbti());

        if (my == null || other == null) return 0;

        int score = MbtiScoreMap.TABLE
                .getOrDefault(my, Map.of())
                .getOrDefault(other, 0);

        log.info("  [MbtiScore] {}({}) → {}({}): {}점",
                me.getUserName(), my,
                target.getUserName(), other,
                score
        );

        return score;
    }

    private String convertToMbtiString(Map<String, String> map) {
        if (map == null) return null;

        try {
            return (map.get("EI")
                    + map.get("SN")
                    + map.get("TF")
                    + map.get("JP")).toUpperCase();
        } catch (Exception e) {
            return null;
        }
    }
}
