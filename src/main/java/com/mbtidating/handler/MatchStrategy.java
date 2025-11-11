package com.mbtidating.handler;

import com.mbtidating.dto.User;
import java.util.List;

public interface MatchStrategy {
    /**
     * 주어진 사용자(me)에 대해 후보 중 가장 적합한 상대를 찾는다.
     */
    User findMatch(User me, List<User> candidates);
}
