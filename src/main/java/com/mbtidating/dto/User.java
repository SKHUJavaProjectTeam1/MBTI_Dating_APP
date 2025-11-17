package com.mbtidating.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "users")
@Data
public class User {

    @Id
    private String _id; // MongoDB의 ObjectId (자동 생성)

    private String id;           // 로그인용 아이디 (user1)
    private String userName;     // 사용자 이름
    private String pwd;          // 비밀번호 해시값
    private String gender;       // "m" / "f"
    private Integer age;
    private String region;

    // 중첩 객체(MBTI 각 항목) -> Map 또는 별도 클래스 가능
    private Map<String, String> mbti;

    private String profileImg;   // 프로필 이미지 파일명

    private Instant createdAt;   // 생성 시간
    private Instant lastLogin;   // 마지막 로그인 시간

    private Tokens tokens;       // 토큰 객체 (내부 static class로 분리)

    @Data
    public static class Tokens {
        private String access;
        private String refresh;
    }
}