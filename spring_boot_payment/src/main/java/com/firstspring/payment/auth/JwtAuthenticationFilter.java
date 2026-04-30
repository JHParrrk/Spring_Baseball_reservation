package com.firstspring.payment.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 인증 필터 (Payment 서비스)
 *
 * Gateway가 발급한 JWT를 검증하여 SecurityContext에 인증 정보를 등록합니다.
 * 토큰 추출 우선순위:
 *   1순위: Authorization: Bearer 헤더 (Swagger / 직접 호출)
 *   2순위: auth_token HTTP-only 쿠키 (브라우저 OAuth2 플로우)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.getEmailFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);

                UserPrincipal principal = new UserPrincipal(userId, email);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        principal, null, List.of(new SimpleGrantedAuthority(role)));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                log.warn("[Payment][JWT] 유효하지 않은 토큰. URI: {}", request.getRequestURI());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (!token.isBlank()) {
                return token;
            }
        }
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("auth_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
