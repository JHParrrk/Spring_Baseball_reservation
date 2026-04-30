package com.firstspring.gateway.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Google OAuth2 인증 후 사용자 정보를 담는 DTO
 *
 * GatewayOAuth2UserService.loadUser() 에서 reservation의 내부 API를 통해
 * DB 사용자 정보(id, role)를 조회한 뒤 이 객체에 담아 반환합니다.
 * OAuth2SuccessHandler 에서 이 객체를 이용해 JWT를 생성합니다.
 */
@Getter
@RequiredArgsConstructor
public class GatewayOAuth2User implements OAuth2User {

    private final Map<String, Object> attributes;
    private final String email;
    private final String role;    // "ROLE_USER" 또는 "ROLE_ADMIN"
    private final Long userId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    /** Spring Security 내부에서 사용하는 식별자 (email 사용) */
    @Override
    public String getName() {
        return email;
    }
}
