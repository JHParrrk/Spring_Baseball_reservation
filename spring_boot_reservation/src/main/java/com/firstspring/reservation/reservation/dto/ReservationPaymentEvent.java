package com.firstspring.reservation.reservation.dto;

import java.time.LocalDateTime;

/**
 * [Kafka] 결제 요청 이벤트 DTO (reservation.payment 토픽 전용)
 *
 * CVC를 포함하며, payment 서비스만 구독하는 reservation.payment 토픽으로 발행됩니다.
 * 알림용 reservation.success 토픽과 분리하여 최소 권한 원칙을 준수합니다.
 *
 * ⚠ PCI-DSS: CVC는 이 이벤트에만 포함되며, 알림(ReservationSuccessEvent)에는 포함되지 않습니다.
 *   payment 서비스가 처리 완료 후 이 값은 절대 영속 저장하지 않습니다.
 */
public record ReservationPaymentEvent(
        Long reservationId,
        Long userId,
        Long seatId,
        String seatNumber,
        String tier,
        String matchTitle,
        String status,
        String cvc,
        LocalDateTime reservedAt) {
}
