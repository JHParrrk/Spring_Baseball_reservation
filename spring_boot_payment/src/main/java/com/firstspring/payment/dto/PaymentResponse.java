package com.firstspring.payment.dto;

import com.firstspring.payment.entity.Payment;

import java.time.LocalDateTime;

/**
 * 결제 조회 응답 DTO
 * Payment 엔티티를 직접 노출하지 않도록 필드를 선별합니다.
 */
public record PaymentResponse(
        Long id,
        Long reservationId,
        Long userId,
        String status,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime processedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getReservationId(),
                payment.getUserId(),
                payment.getStatus().name(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getProcessedAt()
        );
    }
}
