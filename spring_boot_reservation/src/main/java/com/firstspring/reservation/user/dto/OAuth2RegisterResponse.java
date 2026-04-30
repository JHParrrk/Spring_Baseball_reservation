package com.firstspring.reservation.user.dto;

/**
 * 게이트웨이로 반환하는 유저 기본 정보
 * role: "USER" 또는 "ADMIN" (ROLE_ 접두사 없음 — 게이트웨이에서 접두사 추가)
 */
public record OAuth2RegisterResponse(
        Long id,
        String email,
        String role
) {}
