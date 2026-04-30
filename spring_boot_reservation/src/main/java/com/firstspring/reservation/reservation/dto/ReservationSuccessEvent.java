package com.firstspring.reservation.reservation.dto;

import java.time.LocalDateTime;

/**
 * [Kafka] 예약 성공 이벤트 DTO
 *
 * 예약이 성공적으로 생성된 직후 Kafka 토픽으로 발행되는 이벤트입니다.
 *
 * 이벤트 발행/수신 흐름:
 * 예약 성공
 * → ReservationEventPublisher → Kafka Topic: reservation.success
 *   ├─ ReservationNotificationConsumer (알림 발송)
 *   └─ payment 서비스 ReservationEventConsumer (결제 처리)
 *
 * ⚠ cvc 필드는 mock 시뮬레이션 전용입니다.
 *   실제 운영 환경에서는 CVC를 이벤트에 포함하지 마세요 (PCI-DSS 위반).
 */
public record ReservationSuccessEvent(
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
