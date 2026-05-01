package com.firstspring.reservation.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 게이트웨이에서 OAuth2 로그인 후 유저 등록/조회 시 전송하는 요청 DTO
 */
public record OAuth2RegisterRequest(
        @NotBlank(message = "email은 필수 입력값입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,
        String name,
        @NotBlank(message = "provider는 필수 입력값입니다.")
        String provider,
        @NotBlank(message = "providerId는 필수 입력값입니다.")
        String providerId
) {}
