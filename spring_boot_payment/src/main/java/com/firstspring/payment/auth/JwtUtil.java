package com.firstspring.payment.auth;

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

/**
 * JWT 검증 유틸리티 (Payment 서비스 전용)
 *
 * Gateway에서 발급한 JWT를 동일한 시크릿(JWT_SECRET)으로 검증합니다.
 * Payment 서비스는 토큰을 생성하지 않고 검증만 수행합니다.
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET은 최소 32자 이상이어야 합니다. 현재 길이: " +
                    (secret == null ? 0 : secret.length()));
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        String role = getClaims(token).get("role", String.class);
        if (role != null && !role.startsWith("ROLE_")) {
            return "ROLE_" + role;
        }
        return role;
    }

    public Long getUserIdFromToken(String token) {
        Number userId = getClaims(token).get("userId", Number.class);
        return userId != null ? userId.longValue() : null;
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("[Payment][JWT] 만료된 토큰: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("[Payment][JWT] 서명 불일치 — 위조 가능성: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("[Payment][JWT] 잘못된 형식의 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("[Payment][JWT] 지원하지 않는 토큰: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("[Payment][JWT] 빈 Claims: {}", e.getMessage());
        }
        return false;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
