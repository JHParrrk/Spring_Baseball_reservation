package com.firstspring.reservation.reservation.event;

import com.firstspring.reservation.reservation.dto.ReservationResponse;

/**
 * [Spring Events] 결제 개시 내부 이벤트
 *
 * payReservations() @Transactional 메서드 내에서 발행됩니다.
 * @TransactionalEventListener(AFTER_COMMIT) 리스너가 트랜잭션 커밋 완료 후
 * Kafka 이벤트를 안전하게 발행합니다. (유령 메시지 방지)
 */
public record ReservationPaymentInitiatedEvent(ReservationResponse response, String cvc) {
}
