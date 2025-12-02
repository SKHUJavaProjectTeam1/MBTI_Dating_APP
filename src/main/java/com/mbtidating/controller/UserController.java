package com.mbtidating.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.mbtidating.config.JwtUtil;
import com.mbtidating.dto.User;
import com.mbtidating.dto.UserUpdateRequest;
import com.mbtidating.handler.CompositeMatchStrategy;
import com.mbtidating.handler.GenderScoreStrategy;
import com.mbtidating.handler.MbtiScoreStrategy;
import com.mbtidating.model.LoginRequest;
import com.mbtidating.model.SignupRequest;
import com.mbtidating.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 🔹 전체 유저 목록 (테스트용)
    @GetMapping
    public List<User> list() {
        return userRepository.findAll();
    }
    

    // 🔹 회원가입
    @PostMapping
    public User create(@Valid @RequestBody SignupRequest req) {

        // ✅ 1) 로그인 아이디(id) 중복 검사
        String loginId = req.getId();          // SignupView에서 보내는 "id"
        if (userRepository.findByLoginId(loginId).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 사용 중인 아이디입니다."
            );
        }

        // ✅ 2) userName(닉네임) 중복 검사
        String userName = req.getUserName();   // SignupView에서 보내는 "userName"
        if (userRepository.findByUserName(userName).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 사용 중인 닉네임입니다."
            );
        }

        // ✅ 3) User 엔티티 생성 및 비밀번호 해싱
        User user = new User();

        // 로그인 아이디 + 닉네임 분리 저장
        user.setId(loginId);
        user.setUserName(userName);

        // 비밀번호 해싱
        String rawPassword = req.getPwd();
        String hashedPassword = passwordEncoder.encode(rawPassword);
        user.setPwd(hashedPassword);

        user.setGender(req.getGender());
        user.setAge(req.getAge());

        // MBTI 4글자 → Map으로 변환
        if (req.getMbti() != null && req.getMbti().length() == 4) {
            HashMap<String, String> mbtiMap = new HashMap<>();
            mbtiMap.put("EI", String.valueOf(req.getMbti().charAt(0)));
            mbtiMap.put("SN", String.valueOf(req.getMbti().charAt(1)));
            mbtiMap.put("TF", String.valueOf(req.getMbti().charAt(2)));
            mbtiMap.put("JP", String.valueOf(req.getMbti().charAt(3)));
            user.setMbti(mbtiMap);
        }

        user.setProfileImg("default.jpg");
        user.setCreatedAt(Instant.now());
        user.setLastLogin(Instant.now());

        // 토큰은 회원가입 시점에는 빈 값으로 초기화
        User.Tokens tokens = new User.Tokens();
        tokens.setAccess("");
        tokens.setRefresh("");
        user.setTokens(tokens);

        return userRepository.save(user);
    }

    @GetMapping("/recommend/{userId}")
    public List<User> recommend(@PathVariable String userId) {

        // 1) 본인 정보 불러오기
        User me = userRepository.findByLoginId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // 2) 전체 유저 목록
        List<User> all = userRepository.findAll();

        // 3) 스코어 전략 결합
        CompositeMatchStrategy strategy = new CompositeMatchStrategy()
                .add(new MbtiScoreStrategy())
                .add(new GenderScoreStrategy());
                // .add(new AgeScoreStrategy()); → 필요하면 추가

        // 4) 점수 계산 + 스케일업 적용 (색상 문제 해결)
        for (User u : all) {
            if (!u.getId().equals(me.getId())) {
                int score = strategy.calculateScore(me, u);
                u.setMatchRate(score * 10); // 🔥 점수 스케일업
            } else {
                u.setMatchRate(-1);
            }
        }

        // 5) 자기 자신 제외 + 점수 기준 정렬
        List<User> sorted = all.stream()
                .filter(u -> !u.getId().equals(me.getId()))
                .sorted((a, b) -> Integer.compare(b.getMatchRate(), a.getMatchRate()))
                .toList();

        // =========== 🔥 6) 다양성(Variety) 추가 ===========

        // 상위 20%는 유지, 나머지는 랜덤 섞기
        int topCount = Math.max(1, (int)(sorted.size() * 0.2));

        List<User> top = new ArrayList<>(sorted.subList(0, topCount));
        List<User> rest = new ArrayList<>(sorted.subList(topCount, sorted.size()));

        Collections.shuffle(rest);  // 🔥 다양성 추가 (랜덤)

        List<User> finalList = new ArrayList<>();
        finalList.addAll(top);
        finalList.addAll(rest);

        // =========== 🔥 7) 같은 MBTI 3명 이상 제한 ===========

        Map<String, Integer> mbtiLimit = new HashMap<>();
        int maxPerMbti = 3;

        List<User> result = new ArrayList<>();

        for (User u : finalList) {
            String mbti = buildMbti(u.getMbti());
            int count = mbtiLimit.getOrDefault(mbti, 0);

            if (count < maxPerMbti) {
                result.add(u);
                mbtiLimit.put(mbti, count + 1);
            }
        }

        return result;
    }


    // MBTI Map → 문자열 변환
    private String buildMbti(Map<String, String> map) {
        if (map == null) return "NULL";
        try {
            return (map.get("EI") + map.get("SN") + map.get("TF") + map.get("JP")).toUpperCase();
        } catch (Exception e) {
            return "NULL";
        }
    }


    
    // 🔹 로그인
    @PostMapping("/login")
    public User login(@RequestBody LoginRequest req) {

        User user = userRepository.findByLoginId(req.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "아이디 또는 비밀번호가 올바르지 않습니다."
                        ));

        if (user.getPwd() == null ||
            !passwordEncoder.matches(req.getPwd(), user.getPwd())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "아이디 또는 비밀번호가 올바르지 않습니다."
            );
        }

        String accessToken  = JwtUtil.generateAccessToken(user.getId());
        String refreshToken = JwtUtil.generateRefreshToken(user.getId() + "_refresh");

        User.Tokens tokens = new User.Tokens();
        tokens.setAccess(accessToken);
        tokens.setRefresh(refreshToken);
        user.setTokens(tokens);

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        return user;
    }
    
 // 🔹 Refresh 토큰으로 Access 토큰 재발급
    @PostMapping("/refresh")
    public User refreshToken(@RequestBody Map<String, String> body) {
    	System.out.println("🔁 /api/users/refresh 호출됨, body = " + body);
    	
        String refreshToken = body.get("refreshToken");

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "refreshToken이 필요합니다.");
        }

        // 1) 토큰 타입 확인
        String type = JwtUtil.getTokenType(refreshToken);
        if (!"refresh".equals(type)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh 토큰이 아닙니다.");
        }

        // 2) 만료 여부 확인
        if (JwtUtil.isExpired(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh 토큰이 만료되었습니다. 다시 로그인하세요.");
        }

        // 3) 토큰에서 loginId 꺼냄
        String loginId = JwtUtil.extractClaims(refreshToken).getSubject();

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        // 4) 새 Access 토큰(필요하면 Refresh도) 발급
        String newAccessToken  = JwtUtil.generateAccessToken(user.getId());
        String newRefreshToken = JwtUtil.generateRefreshToken(user.getId()); // 토큰 로테이션 정책

        User.Tokens tokens = new User.Tokens();
        tokens.setAccess(newAccessToken);
        tokens.setRefresh(newRefreshToken);
        user.setTokens(tokens);

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        return user;
    }


 // 🔹 프로필 수정 (HomeView.ProfileEditDialog에서 호출)
    @PutMapping("/{id}")
    public User updateProfile(
            @PathVariable("id") String id,
            @RequestBody UserUpdateRequest req) {

        // ✅ 로그인 아이디 기준으로 유저 찾기
        User user = userRepository.findByLoginId(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        // 닉네임(userName) 업데이트
        if (req.getUserName() != null && !req.getUserName().isBlank()) {
            user.setUserName(req.getUserName());
        }

        user.setGender(req.getGender());
        user.setAge(req.getAge());

        if (req.getMbti() != null) {   // NPE 방지용
            user.setMbti(req.getMbti());
        }

        return userRepository.save(user);
    }

}
