package com.firstspring.reservation.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * [스프링 입문] JWT 인증 필터입니다.
 *
 * 모든 HTTP 요청이 컨트롤러에 도달하기 전에 이 필터를 거칩니다.
 * OncePerRequestFilter 를 상속하면 요청당 정확히 한 번만 실행되도록 보장해줍니다.
 *
 * @Component : Bean으로 등록합니다.
 * @RequiredArgsConstructor : final 필드(JwtUtil)를 생성자 주입으로 자동 연결합니다.
 *
 *                          동작 흐름:
 *                          1) Authorization: Bearer <token> 헤더가 있는지 확인
 *                          2) 토큰 유효성 검증 (validateToken)
 *                          3) 토큰에서 email, role, userId 추출
 *                          4) UserPrincipal 객체를 SecurityContextHolder 에 등록
 *                          5) filterChain.doFilter() 로 다음 필터 또는 컨트롤러로 전달
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.getEmailFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);

                // UserPrincipal에 사용자 정보를 담아 SecurityContext에 등록합니다
                // → 컨트롤러에서 @AuthenticationPrincipal UserPrincipal principal 로 바로 당길 수 있습니다
                UserPrincipal principal = new UserPrincipal(userId, email);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        principal, null, List.of(new SimpleGrantedAuthority(role)));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } else {
                log.warn("[JWT] 유효하지 않은 토큰. 요청 URI: {}", request.getRequestURI());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 요청에서 JWT 토큰을 추출합니다.
     * 1순위: Authorization: Bearer 헤더 (Swagger / API 직접 호출)
     * 2순위: auth_token HTTP-only 쿠키 (브라우저 OAuth2 플로우)
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (token.isBlank()) {
                log.warn("[JWT] 빈 토큰이 전달되었습니다.");
                return null;
            }
            return token;
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
