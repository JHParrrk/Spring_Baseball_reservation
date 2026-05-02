package com.firstspring.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 게이트웨이 WebFlux Security 설정
 *
 * Spring Cloud Gateway는 리액티브(WebFlux) 기반이므로
 * 서블릿 기반의 HttpSecurity 대신 ServerHttpSecurity를 사용합니다.
 *
 * 아키텍처:
 * - 프론트엔드 (포트 3000): 로그인 UI 및 예약 애플리케이션
 * - 게이트웨이 (포트 8082): OAuth2 콜백 처리 및 API 라우팅
 * - 각 서비스 (8080, 8081): API 제공
 *
 * 인증 전략:
 * 1. 프론트엔드 로그인 → /oauth2/authorization/google 클릭
 * 2. Google 인증 → 게이트웨이의 /login/oauth2/code/google 콜백
 * 3. JWT 토큰 발급 (HTTP-only 쿠키)
 * 4. 프론트엔드 APP_BASE_URL로 리다이렉트
 * 5. API 요청 시 쿠키의 JWT로 각 서비스에서 검증
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // JWT 기반 API 서버에서는 CSRF 불필요
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchanges -> exchanges
                        // OAuth2 콜백 및 상태 확인 API: 인증 없이 접근 가능
                        .pathMatchers("/oauth2/**", "/api/auth/**")
                        .permitAll()
                        // 액추에이터 (헬스체크): 인증 없이 접근 가능
                        .pathMatchers("/actuator/**")
                        .permitAll()
                        // 나머지 모든 요청 허용 — JWT 인증은 각 하위 서비스에서 처리
                        .anyExchange().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        // loginPage 대신 직접 /oauth2/authorization/google으로 리다이렉트
                        // (프론트엔드에서 로그인 페이지 제공)
                        .authenticationSuccessHandler(oAuth2SuccessHandler)
                        .authenticationFailureHandler(oAuth2FailureHandler))
                .build();
    }
}
