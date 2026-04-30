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
 * OAuth2 로그인 성공 후 JWT를 발급하고 리다이렉트하는 핸들러 (리액티브)
 *
 * 동작 흐름:
 * 1. GatewayOAuth2User에서 email, role, userId 추출
 * 2. JWT 토큰 생성
 * 3. HTTP-only 쿠키(auth_token)로 토큰 전달
 *    - JavaScript에서 접근 불가 → XSS 방어
 *    - SameSite=Lax → CSRF 방어
 * 4. /login/success 로 리다이렉트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements ServerAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Value("${app.base-url:http://localhost:8082}")
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
        exchange.getResponse().getHeaders().setLocation(URI.create(baseUrl + "/login/success"));
        return exchange.getResponse().setComplete();
    }
}
