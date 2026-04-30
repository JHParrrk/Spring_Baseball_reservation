package com.firstspring.gateway.security.dto;

/**
 * reservation 내부 API에서 반환하는 유저 정보
 * role: "USER" 또는 "ADMIN" (ROLE_ 접두사 없음)
 */
public record UserRegistrationResponse(
        Long id,
        String email,
        String role
) {}
