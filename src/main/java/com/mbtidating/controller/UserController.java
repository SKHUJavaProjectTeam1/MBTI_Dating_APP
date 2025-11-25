package com.mbtidating.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

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
                    "이미 사용 중인 사용자명입니다."
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

        String accessToken  = JwtUtil.generateToken(user.getId());
        String refreshToken = JwtUtil.generateToken(user.getId() + "_refresh");

        User.Tokens tokens = new User.Tokens();
        tokens.setAccess(accessToken);
        tokens.setRefresh(refreshToken);
        user.setTokens(tokens);

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        return user;
    }


    // 🔹 프로필 수정 (HomeView.ProfileEditDialog에서 호출)
    @PutMapping("/{id}")
    public User updateProfile(
            @PathVariable("id") String id,   // 여기 id = 로그인 아이디 (User.id)
            @RequestBody UserUpdateRequest req) {

        // ✅ 로그인 아이디 기준으로 유저 찾기
        User user = userRepository.findByLoginId(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        user.setGender(req.getGender());
        user.setAge(req.getAge());
        user.setMbti(req.getMbti());

        return userRepository.save(user);
    }
}
