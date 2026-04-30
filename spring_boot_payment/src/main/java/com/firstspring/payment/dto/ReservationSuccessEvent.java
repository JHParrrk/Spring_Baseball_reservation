package com.firstspring.payment.dto;

import java.time.LocalDateTime;

/**
 * reservation.success 토픽에서 수신하는 이벤트 DTO
 * reservation 서비스의 ReservationSuccessEvent와 동일한 구조로 역직렬화됩니다.
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
