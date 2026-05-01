package com.firstspring.reservation.user.controller;

import com.firstspring.reservation.user.dto.OAuth2RegisterRequest;
import com.firstspring.reservation.user.dto.OAuth2RegisterResponse;
import com.firstspring.reservation.user.entity.User;
import com.firstspring.reservation.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.security.MessageDigest;

/**
 * 게이트웨이에서만 호출하는 내부 전용 컨트롤러
 *
 * 외부에서 직접 접근할 수 없도록 X-Internal-Key 헤더로 요청을 검증합니다.
 * SecurityConfig 에서 /internal/** 는 permitAll()로 설정되어 있으므로
 * JWT 없이도 진입할 수 있지만, 올바른 내부 시크릿이 없으면 403을 반환합니다.
 *
 * 게이트웨이가 이 엔드포인트로 노출하는 라우트를 추가하지 않는 한,
 * 외부에서 직접 접근할 수 없습니다.
 */
@Slf4j
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserRepository userRepository;

    @Value("${internal.secret}")
    private String internalSecret;

    /**
     * Google OAuth2 로그인 사용자를 DB에 등록하거나 기존 유저를 조회합니다.
     *
     * - 신규 유저: DB에 저장 후 정보 반환
     * - 기존 유저: name 업데이트 후 정보 반환
     */
    @PostMapping("/oauth2")
    @Transactional
    public ResponseEntity<OAuth2RegisterResponse> registerOrGetUser(
            @RequestHeader("X-Internal-Key") String requestSecret,
            @Valid @RequestBody OAuth2RegisterRequest request) {

        // MessageDigest.isEqual: 상수 시간 비교로 timing attack 방지
        if (!MessageDigest.isEqual(Utf8.encode(internalSecret), Utf8.encode(requestSecret))) {
            log.warn("[Internal] 잘못된 내부 시크릿으로 접근 시도");
            return ResponseEntity.status(403).build();
        }

        User user = userRepository.findByEmail(request.email()).orElse(null);

        if (user == null) {
            String name = request.name();
            if (name == null || name.isBlank()) {
                name = request.email().contains("@") ? request.email().split("@")[0] : "user";
            }
            user = new User(name, request.email(), request.provider(), request.providerId(), "USER");
            user = userRepository.save(user);
            log.info("[Internal] 신규 유저 등록: {}", request.email());
        } else {
            if (request.name() != null && !request.name().isBlank()) {
                user.updateName(request.name());
                userRepository.save(user);
            }
            log.debug("[Internal] 기존 유저 조회: {}", request.email());
        }

        return ResponseEntity.ok(new OAuth2RegisterResponse(user.getId(), user.getEmail(), user.getRole()));
    }
}
