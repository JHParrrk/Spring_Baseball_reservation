package com.firstspring.reservation.reservation.dto;

/**
 * [RabbitMQ] 예약 취소 지연 메시지 DTO
 *
 * 예약 생성 직후 RabbitMQ pending.queue에 발행됩니다.
 * 5분 후 TTL 만료 → DLX → cancel.queue → Listener 수신 → 자동 취소 처리
 *
 * record를 사용하여 직렬화/역직렬화가 용이하게 구성합니다.
 */
public record ReservationTimeoutMessage(
        Long reservationId,
        Long seatId) {
}
