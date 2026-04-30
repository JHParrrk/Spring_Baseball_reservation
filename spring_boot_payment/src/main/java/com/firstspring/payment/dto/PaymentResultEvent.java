package com.firstspring.payment.dto;

import java.time.LocalDateTime;

/**
 * payment.result 토픽으로 발행하는 결제 결과 이벤트 DTO
 * reservation 서비스가 이 이벤트를 수신하여 예약 상태를 CONFIRMED 또는 CANCELLED로 업데이트합니다.
 *
 * status: "SUCCESS" | "FAILED"
 */
public record PaymentResultEvent(
        Long paymentId,
        Long reservationId,
        Long userId,
        String status,
        String failureReason,
        LocalDateTime processedAt) {
}
