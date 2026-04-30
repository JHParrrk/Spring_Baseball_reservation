package com.firstspring.gateway.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Google OAuth2 인증 성공 후 사용자 정보를 처리하는 리액티브 서비스
 *
 * 흐름:
 * 1. Google에서 사용자 정보(email, name, sub) 수신
 * 2. reservation 서비스의 /internal/users/oauth2 호출
 *    → DB에 신규 유저 등록 또는 기존 유저 조회
 * 3. GatewayOAuth2User 객체 반환 → OAuth2SuccessHandler로 전달
 *
 * DefaultReactiveOAuth2UserService: WebFlux 환경에서 Google API를 호출하는
 * 기본 구현체를 상속합니다 (블로킹 없이 리액티브로 동작).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayOAuth2UserService extends DefaultReactiveOAuth2UserService {

    private final UserRegistrationClient userRegistrationClient;

    @Override
    public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        return super.loadUser(userRequest).flatMap(oAuth2User -> {
            Map<String, Object> attributes = oAuth2User.getAttributes();
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            String provider = userRequest.getClientRegistration().getRegistrationId();
            String providerId = (String) attributes.get("sub");

            if (email == null || email.isBlank()) {
                return Mono.error(new OAuth2AuthenticationException(
                        "OAuth2 프로바이더로부터 이메일 정보를 받지 못했습니다."));
            }

            // name이 없으면 이메일 아이디 부분으로 대체
            if (name == null || name.isBlank()) {
                name = email.contains("@") ? email.split("@")[0] : "user_" + System.currentTimeMillis();
            }

            final String finalName = name;
            return userRegistrationClient.registerOrGetUser(email, finalName, provider, providerId)
                    .map(user -> (OAuth2User) new GatewayOAuth2User(
                            attributes,
                            email,
                            "ROLE_" + user.role(),  // DB에는 "USER"/"ADMIN" 저장 → "ROLE_USER"/"ROLE_ADMIN"
                            user.id()));
        });
    }
}
