package com.firstspring.payment.event;

import com.firstspring.payment.dto.PaymentResultEvent;

/**
 * [Spring Events] 결제 처리 완료 이벤트
 *
 * DB 트랜잭션 커밋 전에 발행되어, @TransactionalEventListener(AFTER_COMMIT)
 * 리스너가 커밋 성공 이후에만 Kafka 메시지를 발행하도록 합니다.
 * 이를 통해 DB 커밋 실패 시 Kafka 메시지만 발행되는 '유령 메시지' 현상을 방지합니다.
 */
public record PaymentProcessedEvent(PaymentResultEvent resultEvent) {
}
