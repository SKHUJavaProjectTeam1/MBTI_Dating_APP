package com.mbtidating.controller;

import com.mbtidating.dto.User;
import com.mbtidating.handler.*;
import com.mbtidating.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchController {

    private final UserRepository userRepository;

    // 전략들을 조합
    private final CompositeMatchStrategy matchStrategy =
            new CompositeMatchStrategy()
                    .add(new GenderScoreStrategy())
                    .add(new MbtiScoreStrategy());

    @PostMapping
    public User match(@RequestBody Map<String, String> req) throws Exception {

        String userName = req.get("userName");
        log.info("[MATCH REQUEST] 요청한 사용자 = {}", userName);
        User me = userRepository.findByUserName(userName)
                .orElseThrow(() -> new Exception("사용자를 찾을 수 없습니다."));

        List<User> all = userRepository.findAll();
        User matched = matchStrategy.findMatch(me, all);

        if (matched == null)
            throw new Exception("매칭 상대를 찾을 수 없습니다.");

        return matched;
    }
}
