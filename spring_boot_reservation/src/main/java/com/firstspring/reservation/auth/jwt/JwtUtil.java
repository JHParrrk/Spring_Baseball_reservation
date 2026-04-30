package com.firstspring.reservation.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * [스프링 입문] JWT(JSON Web Token) 생성/파싱/검증 유틸리티입니다.
 *
 * @Component : 이 클래스를 스프링 Bean으로 등록합니다.
 *            → 다른 Bean이 @Autowired 또는 생성자 주입으로 이 클래스를 가져다 쓸 수 있습니다.
 *
 *            JWT 구조: header.payload.signature (점(.))으로 나뉘어짐)
 *            - header : 알고리즘 정보 (HS256)
 *            - payload : 사용자 정보 (email, role, userId, 만료시간)
 *            - signature: 시크릿키로 서명하여 위조 별조 방지
 *
 * @Value : application.properties의 jwt.secret, jwt.expiration-ms 값을 자동 주입합니다.
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
        // JWT Secret은 HMAC-SHA256 최소 보안 요건인 32자(256비트) 이상이어야 합니다
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET은 최소 32자 이상이어야 합니다. 현재 길이: " +
                    (secret == null ? 0 : secret.length()));
        }
        // HMAC-SHA 알고리즘에 사용할 시크릿키를 만듭니다
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /** 토큰에서 이메일(subject)을 꼽아냅니다. */
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * 토큰에서 사용자 권한(ROLE_USER / ROLE_ADMIN)을 꼽아냅니다.
     * 항상 "ROLE_" 접두사가 붙은 형태로 반환하여 SimpleGrantedAuthority와의 일관성을 보장합니다.
     */
    public String getRoleFromToken(String token) {
        String role = getClaims(token).get("role", String.class);
        if (role != null && !role.startsWith("ROLE_")) {
            return "ROLE_" + role;
        }
        return role;
    }

    /** 토큰에서 사용자 ID(DB PK)를 꼽아냅니다. */
    public Long getUserIdFromToken(String token) {
        // 숫자 타입 역직렬화 시 Integer로 읽힐 수 있으므로 Number로 받고 변환합니다
        Number userId = getClaims(token).get("userId", Number.class);
        return userId != null ? userId.longValue() : null;
    }

    /**
     * 토큰이 유효한지 확인합니다.
     * 예외 종류별로 원인을 로그에 남겨 보안 모니터링을 지원합니다.
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("JWT 서명 불일치 — 위조된 토큰 가능성: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("잘못된 형식의 JWT 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 JWT 토큰: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT Claims 문자열이 비어있습니다: {}", e.getMessage());
        }
        return false;
    }

    /** 토큰에서 전체 Claims(페이로드 정보)를 디코딩합니다. 서명 검증이 실패하면 예외가 발생합니다. */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
