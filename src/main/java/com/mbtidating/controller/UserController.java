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

     // 🔥 default 이미지면 서버에서 랜덤 이미지 부여
        if (req.getProfileImg() == null || req.getProfileImg().isBlank() || req.getProfileImg().equals("default.jpg")) {
            int random = 1 + (int)(Math.random() * 5);
            user.setProfileImg(String.valueOf(random));
        } else {
            user.setProfileImg(req.getProfileImg());
        }


        user.setCreatedAt(Instant.now());
        user.setLastLogin(Instant.now());

        User.Tokens tokens = new User.Tokens();
        tokens.setAccess("");
        tokens.setRefresh("");
        user.setTokens(tokens);

        return userRepository.save(user);
    }
    
    @GetMapping("/{id}")
    public User getUserById(@PathVariable String id) {
        return userRepository.findByLoginId(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    }


    @GetMapping("/recommend/{userId}")
    public List<User> recommend(@PathVariable String userId) {

        // 1) 본인 정보 불러오기
        User me = userRepository.findByLoginId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // 2) 전체 유저 목록
        List<User> all = userRepository.findAll();

        // 2-1) 성별 정규화
        String myGender = normalizeGender(me.getGender());

        // 2-2) 성별 필터링 (성별이 맞지 않는 사람 제외)
        List<User> genderFiltered = all.stream()
                .filter(u -> !u.getId().equals(me.getId()))
                .filter(u -> {
                    String g = normalizeGender(u.getGender());

                    if (myGender.equals("m")) return g.equals("f"); // 남자는 여자만
                    if (myGender.equals("f")) return g.equals("m"); // 여자는 남자만
                    return g.equals("m") || g.equals("f");          // 기타의 경우 양쪽
                })
                .toList();

        // 3) 스코어 전략 결합
        CompositeMatchStrategy strategy = new CompositeMatchStrategy()
                .add(new MbtiScoreStrategy());

        if (!myGender.equals("other")) { // 남/여일 때만 성별 점수
            strategy.add(new GenderScoreStrategy());
        }

        // 4) 점수 계산 + 스케일업
        for (User u : genderFiltered) {
            int score = strategy.calculateScore(me, u);
            u.setMatchRate(score * 10);
        }

        // 5) 점수 기준 정렬
        List<User> sorted = genderFiltered.stream()
                .sorted((a, b) -> Integer.compare(b.getMatchRate(), a.getMatchRate()))
                .toList();

        // 6) 상위 20% + 나머지 랜덤
        int topCount = Math.max(1, (int) (sorted.size() * 0.2));
        List<User> top = new ArrayList<>(sorted.subList(0, topCount));
        List<User> rest = new ArrayList<>(sorted.subList(topCount, sorted.size()));
        Collections.shuffle(rest);

        List<User> finalList = new ArrayList<>();
        finalList.addAll(top);
        finalList.addAll(rest);

        // 7) 같은 MBTI 3명 이상 제한
        Map<String, Integer> mbtiCount = new HashMap<>();
        int maxPerMbti = 3;
        List<User> result = new ArrayList<>();

        for (User u : finalList) {
            String mbti = buildMbti(u.getMbti());
            int count = mbtiCount.getOrDefault(mbti, 0);

            if (count < maxPerMbti) {
                result.add(u);
                mbtiCount.put(mbti, count + 1);
            }
        }

        // 8) 🔥 profileImg가 default or null이면 랜덤 이미지 적용 + DB에 저장
        for (User u : result) {
            if (u.getProfileImg() == null
                    || u.getProfileImg().isBlank()
                    || u.getProfileImg().equals("default.jpg")) {

                int random = 1 + (int)(Math.random() * 5);
                u.setProfileImg(String.valueOf(random));
                userRepository.save(u);     // 🔥 영구 저장
            }
        }

        return result;
    }



    // 🔥 성별 정규화 함수
    private String normalizeGender(String g) {
        if (g == null) return "other";
        g = g.trim().toLowerCase();

        if (g.startsWith("남") || g.equals("m") || g.equals("male"))
            return "m";
        if (g.startsWith("여") || g.equals("f") || g.equals("female"))
            return "f";

        return "other"; // 기타
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

        User user = userRepository.findByLoginId(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        // 닉네임 업데이트
        if (req.getUserName() != null && !req.getUserName().isBlank()) {
            user.setUserName(req.getUserName());
        }

        // 성별 업데이트
        if (req.getGender() != null && !req.getGender().isBlank()) {
            user.setGender(req.getGender());
        }

        // 나이 업데이트
        if (req.getAge() != null) {
            user.setAge(req.getAge());
        }

        // 프로필 이미지 업데이트
        if (req.getProfileImg() != null && !req.getProfileImg().isBlank()) {
            user.setProfileImg(req.getProfileImg());
        }

        // MBTI 업데이트 (null이면 기존값 유지)
        if (req.getMbti() != null && !req.getMbti().isEmpty()) {
            user.setMbti(req.getMbti());
        }

        return userRepository.save(user);
    }



}
