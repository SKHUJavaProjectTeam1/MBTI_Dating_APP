package com.mbtidating.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

//import org.glassfish.grizzly.http.util.HttpStatus;
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

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;



@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    public List<User> list() {
        return userRepository.findAll();
    }

    @PostMapping
    public User create(@Valid @RequestBody SignupRequest userEdit) {
        User user = new User();
        user.setId(userEdit.getUserName());
        user.setUserName(userEdit.getUserName());
        user.setPwd(userEdit.getPwd());
        user.setGender(userEdit.getGender());
        user.setAge(userEdit.getAge());

        if (userEdit.getMbti() != null && userEdit.getMbti().length() == 4) {
            HashMap<String, String> mbtiMap = new HashMap<>();
            mbtiMap.put("EI", String.valueOf(userEdit.getMbti().charAt(0)));
            mbtiMap.put("SN", String.valueOf(userEdit.getMbti().charAt(1)));
            mbtiMap.put("TF", String.valueOf(userEdit.getMbti().charAt(2)));
            mbtiMap.put("JP", String.valueOf(userEdit.getMbti().charAt(3)));
            user.setMbti(mbtiMap);
        }

        user.setProfileImg("default.jpg");
        user.setCreatedAt(Instant.now());
        user.setLastLogin(Instant.now());

        User.Tokens tokens = new User.Tokens();
        tokens.setAccess("");
        tokens.setRefresh("");
        user.setTokens(tokens);

        return userRepository.save(user);
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginRequest req) throws Exception {
        User user = userRepository.findByUserName(req.getUserName())
                .filter(u -> u.getPwd() != null && u.getPwd().equals(req.getPwd()))
                .orElseThrow(() -> new Exception("아이디 또는 비밀번호가 올바르지 않습니다."));

        String accessToken = JwtUtil.generateToken(user.getUserName());
        String refreshToken = JwtUtil.generateToken(user.getUserName() + "_refresh");

        User.Tokens tokens = new User.Tokens();
        tokens.setAccess(accessToken);
        tokens.setRefresh(refreshToken);
        user.setTokens(tokens);

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        return user;
    }
    
 // 🔹 프로필 수정 API
    @PutMapping("/{id}")
    public User updateProfile(
            @PathVariable("id") String id,   // 로그인용 id 기준이라고 가정
            @RequestBody UserUpdateRequest req) {

        // ⚠ 여기서 findById는 "로그인용 id" 기준으로 찾는 메소드여야 해
        // _id(ObjectId) 기준이면, 레포지토리에서 따로 메소드 만들어야 함.
        User user = userRepository.findById(id)
        		.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));


        user.setGender(req.getGender());
        user.setAge(req.getAge());
        user.setMbti(req.getMbti());

        return userRepository.save(user);
    }
}
