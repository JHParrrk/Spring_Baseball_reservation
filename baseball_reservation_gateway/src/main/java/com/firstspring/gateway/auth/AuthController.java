package com.firstspring.gateway.auth;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 인증 관련 엔드포인트
 *
 * 로그아웃 시 게이트웨이가 auth_token 쿠키를 만료시켜
 * 브라우저 인증 상태를 완전히 제거합니다.
 */
@RestController
public class AuthController {

    @PostMapping("/api/auth/logout")
    public Mono<ResponseEntity<Void>> logout(ServerWebExchange exchange) {
        boolean isSecure = "https".equalsIgnoreCase(
            exchange.getRequest().getHeaders().getFirst("X-Forwarded-Proto"))
            || exchange.getRequest().getSslInfo() != null;

        ResponseCookie deleteCookie = ResponseCookie.from("auth_token", "")
                .httpOnly(true)
            .secure(isSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        exchange.getResponse().addCookie(deleteCookie);
        return Mono.just(ResponseEntity.noContent().build());
    }
}
