package com.firstspring.payment.config;

import com.firstspring.payment.auth.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

/**
 * Payment 서비스 Security 설정
 *
 * Gateway가 발급한 JWT를 검증하여 /api/payments/** 접근을 제어합니다.
 * - Swagger: 인증 없이 허용
 * - H2 콘솔, Actuator: dev 프로파일에서만 인증 없이 허용
 * - /api/payments/**: 인증 필요 (ROLE_USER 이상)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final Environment environment;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // 문서 엔드포인트
                    auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll();
                    // 개발 프로파일에서만 로컬 도구 공개
                    if (isDevProfile()) {
                        auth.requestMatchers("/h2-console/**", "/actuator/**").permitAll();
                    }
                    // 결제 API: 인증 필요
                    auth.requestMatchers("/api/payments/**").authenticated();
                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"error\":\"Unauthorized\",\"message\":\"JWT 토큰이 필요합니다.\"}");
                        }))
                // H2 콘솔 iframe 허용
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

        private boolean isDevProfile() {
                return Arrays.stream(environment.getActiveProfiles())
                                .anyMatch(profile -> "dev".equalsIgnoreCase(profile));
        }
}
