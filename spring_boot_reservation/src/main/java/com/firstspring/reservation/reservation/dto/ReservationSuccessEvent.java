package com.firstspring.reservation.reservation.dto;

import java.time.LocalDateTime;

/**
 * [Kafka] 예약 알림 이벤트 DTO (reservation.success 토픽 전용)
 *
 * 사용자 알림(이메일/SMS/푸시) 목적으로 발행됩니다.
 * CVC는 포함하지 않습니다 — 알림 서비스는 CVC가 필요 없으며, 최소 권한 원칙을 준수합니다.
 *
 * 결제 처리용 이벤트는 ReservationPaymentEvent (reservation.payment 토픽) 를 사용하세요.
 */
public record ReservationSuccessEvent(
        Long reservationId,
        Long userId,
        Long seatId,
        String seatNumber,
        String tier,
        String matchTitle,
        String status,
        LocalDateTime reservedAt) {
}
