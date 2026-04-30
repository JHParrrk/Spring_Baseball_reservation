package com.firstspring.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성 유틸리티 (게이트웨이 전용)
 *
 * reservation 서비스와 동일한 시크릿(JWT_SECRET)을 공유합니다.
 * - 게이트웨이: OAuth2 로그인 후 JWT 생성
 * - reservation: 매 요청마다 JWT 서명 검증
 *
 * 두 서비스가 같은 시크릿으로 서명/검증하므로 별도의 키 교환 없이 동작합니다.
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET은 최소 32자 이상이어야 합니다. 현재 길이: " +
                    (secret == null ? 0 : secret.length()));
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * OAuth2 로그인 성공 시 JWT 토큰을 생성합니다.
     * payload: email(subject), role, userId
     */
    public String generateToken(String email, String role, Long userId) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey)
                .compact();
    }
}
