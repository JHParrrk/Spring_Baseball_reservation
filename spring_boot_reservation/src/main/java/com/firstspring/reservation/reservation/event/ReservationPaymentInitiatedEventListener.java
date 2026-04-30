package com.firstspring.reservation.reservation.event;

import com.firstspring.reservation.reservation.service.ReservationEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * [Spring Events] 결제 개시 이벤트 리스너
 *
 * @TransactionalEventListener(phase = AFTER_COMMIT):
 *   DB 트랜잭션이 성공적으로 커밋된 이후에만 실행됩니다.
 *   롤백 시 이 리스너는 호출되지 않으므로 '유령 메시지(DB 실패 후 Kafka 발행)' 현상을 방지합니다.
 *
 * 발행 이벤트:
 * 1. reservation.success (CVC 미포함) → ReservationNotificationConsumer (알림 전용)
 * 2. reservation.payment (CVC 포함)   → payment 서비스 ReservationEventConsumer (결제 전용)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationPaymentInitiatedEventListener {

    private final ReservationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentInitiated(ReservationPaymentInitiatedEvent event) {
        Long reservationId = event.response().id();
        log.info("[Event] DB 커밋 확인 — 결제 Kafka 이벤트 발행. reservationId={}", reservationId);

        // 1. 알림 이벤트 발행 (reservation.success, CVC 미포함)
        try {
            eventPublisher.publishNotification(event.response());
        } catch (Exception e) {
            log.error("[Event] 알림 Kafka 이벤트 발행 실패. reservationId={}", reservationId, e);
        }

        // 2. 결제 이벤트 발행 (reservation.payment, CVC 포함) — payment 서비스 전용
        try {
            eventPublisher.publishPaymentRequest(event.response(), event.cvc());
        } catch (Exception e) {
            log.error("[Event] ⚠️ 결제 Kafka 이벤트 발행 실패 — 결제 미처리 가능. " +
                    "reservationId={}. 수동 복구 또는 DLT 확인이 필요합니다.", reservationId, e);
        }
    }
}
