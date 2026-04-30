package com.firstspring.gateway.security.dto;

/**
 * 게이트웨이 → reservation 내부 API로 유저 등록/조회 요청 시 전송하는 DTO
 */
public record UserRegistrationRequest(
        String email,
        String name,
        String provider,
        String providerId
) {}
