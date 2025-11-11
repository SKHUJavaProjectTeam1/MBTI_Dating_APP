package com.mbtidating.controller;

import com.mbtidating.dto.User;
import com.mbtidating.handler.*;
import com.mbtidating.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchController {

    private final UserRepository userRepository;

    // 전략들을 조합
    private final MatchStrategy matchStrategy = new CompositeMatchStrategy()
            .add(new GenderFilterStrategy())      // 1️⃣ 성별이 다른 상대 먼저 탐색
            .add(new OppositeMBTIStrategy());     // 2️⃣ MBTI 반대 성향 매칭

    @PostMapping
    public User match(@RequestBody Map<String, String> req) throws Exception {
        String userName = req.get("userName");
        User me = userRepository.findByUserName(userName)
                .orElseThrow(() -> new Exception("사용자를 찾을 수 없습니다."));

        List<User> all = userRepository.findAll();
        User matched = matchStrategy.findMatch(me, all);

        if (matched == null)
            throw new Exception("매칭 상대를 찾을 수 없습니다.");

        return matched;
    }
}
