package com.firstspring.reservation.user.dto;

public record UserSummaryResponse(
        Long id,
        String name,
        String email,
        String role,
        String status,
        long activeReservations,
        long cancelledReservations) {
}
