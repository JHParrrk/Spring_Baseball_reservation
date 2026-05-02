package com.firstspring.gateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * OAuth2 인증 실패 핸들러
 *
 * Google 로그인 실패 시 (잘못된 OAuth2 코드, 네트워크 오류 등)
 * 명확한 에러 응답을 클라이언트에 전달합니다.
 *
 * 실패 원인:
 * - 사용자가 Google 로그인 거부
 * - 잘못된 OAuth2 코드
 * - reservation 서비스 유저 등록/조회 실패
 */
@Slf4j
@Component
public class OAuth2FailureHandler implements ServerAuthenticationFailureHandler {

    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange,
                                              AuthenticationException exception) {
        log.warn("[OAuth2] 인증 실패: {}", exception.getMessage(), exception);

        var response = webFilterExchange.getExchange().getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        String jsonResponse = """
                {
                  "error": "authentication_failed",
                  "message": "OAuth2 인증에 실패했습니다.",
                  "details": "%s"
                }
                """.formatted(exception.getMessage() != null ? exception.getMessage() : "Unknown error");

        var buffer = response.bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
