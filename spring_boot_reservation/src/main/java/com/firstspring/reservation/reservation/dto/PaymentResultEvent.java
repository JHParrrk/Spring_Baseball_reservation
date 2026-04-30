package com.firstspring.reservation.reservation.dto;

import java.time.LocalDateTime;

/**
 * [Kafka] payment.result 토픽에서 수신하는 결제 결과 이벤트 DTO
 *
 * payment 서비스가 결제 처리 후 발행하는 이벤트입니다.
 * PaymentResultConsumer가 이 이벤트를 수신하여 예약 상태를 업데이트합니다.
 *
 * status: "SUCCESS" → 예약 CONFIRMED
 *         "FAILED"  → 예약 CANCELLED (좌석 AVAILABLE 복원)
 */
public record PaymentResultEvent(
        Long paymentId,
        Long reservationId,
        Long userId,
        String status,
        String failureReason,
        LocalDateTime processedAt) {
}
