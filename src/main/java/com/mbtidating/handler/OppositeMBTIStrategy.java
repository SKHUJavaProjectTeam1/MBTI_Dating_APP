package com.mbtidating.handler;

import com.mbtidating.dto.User;
import java.util.*;

public class OppositeMBTIStrategy implements MatchStrategy {

    @Override
    public User findMatch(User me, List<User> candidates) {
        Map<String, String> my = me.getMbti();
        if (my == null) return null;

        Map<String, String> opposite = new HashMap<>();
        for (Map.Entry<String, String> e : my.entrySet()) {
            opposite.put(e.getKey(), oppositeOf(e.getValue()));
        }

        User best = null;
        int bestScore = -1;
        for (User u : candidates) {
            if (u.getUserName().equals(me.getUserName())) continue;
            Map<String, String> mbti = u.getMbti();
            if (mbti == null) continue;

            int score = 0;
            for (String k : opposite.keySet()) {
                if (opposite.get(k).equalsIgnoreCase(mbti.get(k))) score++;
            }
            if (score > bestScore) {
                bestScore = score;
                best = u;
            }
        }
        return best;
    }

    private String oppositeOf(String v) {
        if (v == null) return "";
        return switch (v.toUpperCase()) {
            case "E" -> "I"; case "I" -> "E";
            case "S" -> "N"; case "N" -> "S";
            case "T" -> "F"; case "F" -> "T";
            case "J" -> "P"; case "P" -> "J";
            default -> v;
        };
    }
}
