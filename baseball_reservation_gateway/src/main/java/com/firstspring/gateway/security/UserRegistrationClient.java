package com.firstspring.gateway.security;

import com.firstspring.gateway.security.dto.UserRegistrationRequest;
import com.firstspring.gateway.security.dto.UserRegistrationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 게이트웨이 → reservation 내부 API 호출 클라이언트
 *
 * POST /internal/users/oauth2 를 호출하여 Google 소셜 로그인 사용자를
 * reservation DB에 등록하거나 기존 사용자를 조회합니다.
 *
 * X-Internal-Key 헤더로 내부 서비스 요청임을 인증합니다.
 * 이 값은 reservation 서비스의 INTERNAL_SECRET 환경변수와 동일해야 합니다.
 */
@Slf4j
@Component
public class UserRegistrationClient {

    private final WebClient webClient;
    private final String internalSecret;

    public UserRegistrationClient(
            @Value("${reservation.service.url:http://localhost:8080}") String reservationUrl,
            @Value("${internal.secret:internal-secret-key}") String internalSecret,
            WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(reservationUrl).build();
        this.internalSecret = internalSecret;
    }

    public Mono<UserRegistrationResponse> registerOrGetUser(
            String email, String name, String provider, String providerId) {
        return webClient.post()
                .uri("/internal/users/oauth2")
                .header("X-Internal-Key", internalSecret)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserRegistrationRequest(email, name, provider, providerId))
                .retrieve()
                .bodyToMono(UserRegistrationResponse.class)
                .doOnError(e -> log.error("[OAuth2] reservation 유저 등록/조회 실패: {}", e.getMessage()))
                .onErrorMap(e -> !(e instanceof OAuth2AuthenticationException),
                        e -> new OAuth2AuthenticationException(
                                new OAuth2Error("user_registration_failed"),
                                "reservation 서비스 유저 등록/조회 실패: " + e.getMessage(), e));
    }
}
