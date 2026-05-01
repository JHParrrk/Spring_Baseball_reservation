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

        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                resultPublisher.publishResult(event.resultEvent());
                return;
            } catch (Exception e) {
                log.warn("[Event] Kafka 결제 결과 발행 실패 (시도 {}/{}). reservationId={}",
                        attempt, maxAttempts, event.resultEvent().reservationId(), e);
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(500L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        log.error("[Event] Kafka 결제 결과 이벤트 발행 최종 실패 (3회 재시도). reservationId={}. 수동 복구가 필요합니다.",
                event.resultEvent().reservationId());
    }
}
