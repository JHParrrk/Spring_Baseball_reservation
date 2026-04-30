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
 * 인증 전략:
 * - OAuth2 로그인은 게이트웨이에서 처리 (이 설정)
 * - API 요청의 JWT 인증은 각 서비스(reservation/payment)에서 처리
 * - 게이트웨이는 모든 API 요청을 permitAll()로 통과시키고 라우팅만 담당
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // JWT 기반 API 서버에서는 CSRF 불필요
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchanges -> exchanges
                        // 로그인 페이지, OAuth2 콜백, 액추에이터는 인증 없이 접근 가능
                        .pathMatchers("/login", "/login/**", "/oauth2/**", "/actuator/**")
                        .permitAll()
                        // 나머지 모든 요청 허용 — JWT 인증은 각 하위 서비스에서 처리
                        .anyExchange().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        // 커스텀 login 페이지 경로를 명시하여 Security 기본 핸들러와의 충돌을 방지합니다
                        .loginPage("/login")
                        // GatewayOAuth2UserService Bean이 ReactiveOAuth2UserService를 구현하므로
                        // Spring Security가 자동으로 감지하여 사용합니다 (Spring Security 6.x)
                        .authenticationSuccessHandler(oAuth2SuccessHandler))
                .build();
    }
}
