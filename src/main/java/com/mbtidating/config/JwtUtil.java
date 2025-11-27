package com.mbtidating.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    private static final String SECRET = "ThisIsASecretKeyForJwtGenerationExample1234567890";
    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // Access 1시간
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60;
    // Refresh 7일
    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7;

    // 공통 토큰 생성
    private static String buildToken(String username, long expirationMillis, String tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", tokenType); // "access" / "refresh"

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)   // 여기에는 loginId 넣는 중
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Access 토큰 생성
    public static String generateAccessToken(String username) {
        return buildToken(username, ACCESS_TOKEN_EXPIRATION, "access");
    }

    // ✅ Refresh 토큰 생성
    public static String generateRefreshToken(String username) {
        return buildToken(username, REFRESH_TOKEN_EXPIRATION, "refresh");
    }

    // Claims 꺼내기
    public static Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 만료 여부
    public static boolean isExpired(String token) {
        try {
            Date expiration = extractClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    // 토큰 타입 (access / refresh)
    public static String getTokenType(String token) {
        try {
            return (String) extractClaims(token).get("type");
        } catch (Exception e) {
            return null;
        }
    }

    // 기존 validateToken 대체용 – username 리턴 or null
    public static String validateToken(String token) {
        try {
            if (isExpired(token)) return null;
            return extractClaims(token).getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
