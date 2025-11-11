package com.mbtidating.handler;

import com.mbtidating.dto.User;
import java.util.List;

/**
 * 성별이 다른 사람끼리 매칭되도록 하는 전략.
 * (필요에 따라 same-gender, any-gender 등으로 바꿀 수 있음)
 */
public class GenderFilterStrategy implements MatchStrategy {

    @Override
    public User findMatch(User me, List<User> candidates) {
        for (User u : candidates) {
            if (u.getUserName().equals(me.getUserName())) continue;
            if (u.getGender() == null || me.getGender() == null) continue;

            if (!u.getGender().equalsIgnoreCase(me.getGender())) {
                return u; // 반대 성별 중 첫 번째 후보 반환
            }
        }
        return null;
    }
}
