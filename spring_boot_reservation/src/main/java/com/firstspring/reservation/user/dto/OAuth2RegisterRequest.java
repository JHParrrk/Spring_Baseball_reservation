package com.firstspring.reservation.user.dto;

/**
 * 게이트웨이에서 OAuth2 로그인 후 유저 등록/조회 시 전송하는 요청 DTO
 */
public record OAuth2RegisterRequest(
        String email,
        String name,
        String provider,
        String providerId
) {}
