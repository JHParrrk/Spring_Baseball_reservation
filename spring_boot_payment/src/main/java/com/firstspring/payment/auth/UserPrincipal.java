package com.firstspring.payment.auth;

/**
 * 인증된 사용자 정보를 담는 Principal 객체
 *
 * JwtAuthenticationFilter에서 JWT 파싱 후 SecurityContextHolder에 등록됩니다.
 * 컨트롤러에서 @AuthenticationPrincipal UserPrincipal principal 로 사용자 정보를 주입받을 수 있습니다.
 */
public class UserPrincipal {

    private final Long userId;
    private final String email;

    public UserPrincipal(Long userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
