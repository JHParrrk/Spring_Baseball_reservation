package com.firstspring.reservation.user.controller;

import com.firstspring.reservation.auth.jwt.UserPrincipal;
import com.firstspring.reservation.common.exception.custom.ResourceNotFoundException;
import com.firstspring.reservation.user.dto.UserResponse;
import com.firstspring.reservation.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name = "User API", description = "사용자 프로필 조회 API")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    @Operation(summary = "내 프로필 조회", description = "JWT 토큰으로 인증된 본인의 이름, 이메일, 역할, 계정 상태를 조회합니다.")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return userRepository.findById(principal.getUserId())
                .map(UserResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("사용자 정보를 찾을 수 없습니다."));
    }
}
