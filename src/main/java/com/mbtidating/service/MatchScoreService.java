package com.mbtidating.service;
import com.mbtidating.handler.CompositeMatchStrategy;
import com.mbtidating.handler.GenderScoreStrategy;
import com.mbtidating.handler.MbtiScoreStrategy;
import org.springframework.stereotype.Service;
import com.mbtidating.dto.User;

@Service
public class MatchScoreService {

    private final CompositeMatchStrategy composite;

    public MatchScoreService() {
        composite = new CompositeMatchStrategy()
                .add(new MbtiScoreStrategy())
                .add(new GenderScoreStrategy());

    }

    public int calculate(User a, User b) {
        return composite.calculateScore(a, b);
    }
}
