package com.firstspring.reservation.reservation.event;

import com.firstspring.reservation.reservation.dto.ReservationResponse;

/**
 * [Spring Events] 예약 생성 완료 이벤트
 *
 * DB 트랜잭션 커밋 전에 발행되어, @TransactionalEventListener(AFTER_COMMIT)
 * 리스너가 커밋 성공 이후에만 RabbitMQ·Kafka 메시지를 보내도록 합니다.
 * 이를 통해 DB 커밋 실패 시 메시지만 발행되는 '유령 메시지' 현상을 방지합니다.
 *
 * @param response 방금 생성된 예약 응답 DTO
 * @param seatId   타임아웃 메시지에 필요한 좌석 ID (RabbitMQ 발행에 사용)
 * 결제는 사용자가 '내 예약'에서 직접 개시하므로 Kafka 발행은 여기서 하지 않습니다.
 */
public record ReservationCreatedEvent(ReservationResponse response, Long seatId) {
}
