package com.firstspring.payment.event;

import com.firstspring.payment.service.PaymentResultPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * [Spring Events] 결제 처리 완료 이벤트 리스너
 *
 * @TransactionalEventListener(phase = AFTER_COMMIT):
 * DB 트랜잭션이 성공적으로 커밋된 이후에만 Kafka 메시지를 발행합니다.
 * 트랜잭션 롤백 시 이 리스너는 호출되지 않아 유령 메시지를 방지합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProcessedEventListener {

    private final PaymentResultPublisher resultPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        log.info("[Event] DB 커밋 확인 — Kafka payment.result 발행 시작. reservationId={}",
                event.resultEvent().reservationId());
        try {
            resultPublisher.publishResult(event.resultEvent());
        } catch (Exception e) {
            log.error("[Event] Kafka 결제 결과 이벤트 발행 실패. reservationId={}. 상위 재처리/장애 감지 경로로 예외를 전파합니다.",
                    event.resultEvent().reservationId(), e);
            // 실패를 호출 측으로 전파하여 상위 재처리/장애 감지 경로에서 처리할 수 있도록 합니다.
            throw e;
        }
    }
}
