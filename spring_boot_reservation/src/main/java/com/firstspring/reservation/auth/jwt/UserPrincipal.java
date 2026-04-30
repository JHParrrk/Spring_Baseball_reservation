package com.firstspring.reservation.auth.jwt;

/**
 * [스프링 입문] 로그인한 사용자 정보를 담는 객체(Principal)입니다.
 *
 * JwtAuthenticationFilter에서 JWT를 파싱한 후 SecurityContextHolder에 등록됩니다.
 * 컨트롤러에서 @AuthenticationPrincipal UserPrincipal principal 로
 * 로그인 사용자의 userId와 email을 바로 가져다 쓸 수 있습니다.
 *
 * 펼지인터 POJO로 만들어졌으며, 인터페이스 구현 없이도 스프링 Security에서
 * principal 객체로 사용할 수 있습니다.
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
