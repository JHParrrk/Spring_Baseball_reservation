package com.firstspring.gateway.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

/**
 * OAuth2 로그인 성공 후 JWT를 발급하고 프론트엔드로 리다이렉트하는 핸들러 (리액티브)
 *
 * 동작 흐름:
 * 1. Google에서 사용자 정보 수신 → GatewayOAuth2UserService로 DB 등록/조회
 * 2. JWT 토큰 생성
 * 3. HTTP-only 쿠키(auth_token)로 토큰 전달
 *    - JavaScript에서 접근 불가 → XSS 방어
 *    - SameSite=Lax → CSRF 방어
 * 4. 프론트엔드 APP_BASE_URL (http://localhost:3000) 로 리다이렉트
 *
 * 쿠키에 저장된 JWT는 이후 모든 API 요청에 자동으로 첨부됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements ServerAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Value("${app.base-url:http://localhost:3000}")
    private String baseUrl;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange,
                                               Authentication authentication) {
        GatewayOAuth2User oAuth2User = (GatewayOAuth2User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(
                oAuth2User.getEmail(),
                oAuth2User.getRole(),
                oAuth2User.getUserId());

        log.info("[OAuth2] 로그인 성공 — email: {}, role: {}", oAuth2User.getEmail(), oAuth2User.getRole());

        ServerWebExchange exchange = webFilterExchange.getExchange();
        // X-Forwarded-Proto 우선 확인 (리버스 프록시/ALB 뒤에서 getSslInfo()가 null 반환 가능)
        boolean isSecure = "https".equalsIgnoreCase(
                exchange.getRequest().getHeaders().getFirst("X-Forwarded-Proto"))
                || exchange.getRequest().getSslInfo() != null;

        ResponseCookie cookie = ResponseCookie.from("auth_token", token)
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .sameSite("Lax")
                .build();

        exchange.getResponse().addCookie(cookie);
        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
        // 프론트엔드 애플리케이션으로 리다이렉트 (JWT는 쿠키에 저장됨)
        exchange.getResponse().getHeaders().setLocation(URI.create(baseUrl));
        return exchange.getResponse().setComplete();
    }
}
