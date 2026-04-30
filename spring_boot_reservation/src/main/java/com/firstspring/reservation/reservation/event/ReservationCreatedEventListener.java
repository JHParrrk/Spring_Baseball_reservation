package com.firstspring.reservation.reservation.event;

import com.firstspring.reservation.reservation.service.ReservationTimeoutPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * [Spring Events] 예약 생성 이벤트 리스너
 *
 * @TransactionalEventListener(phase = AFTER_COMMIT):
 *                                   DB 트랜잭션이 성공적으로 커밋된 이후에만 이 메서드가 실행됩니다.
 *                                   트랜잭션이 롤백되면 이 리스너는 호출되지 않으므로
 *                                   '유령 메시지(DB 실패 후 메시지 발행)' 현상이 원천 차단됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationCreatedEventListener {

    private final ReservationTimeoutPublisher timeoutPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationCreated(ReservationCreatedEvent event) {
        Long reservationId = event.response().id();
        Long seatId = event.seatId();

        log.info("[Event] DB 커밋 확인 — RabbitMQ 5분 타임아웃 메시지 발행. reservationId={}", reservationId);

        // RabbitMQ: 5분 타임아웃 메시지 발행
        // 5분 내에 결제(Kafka 발행)가 없으면 DLX 경유 자동 CANCELLED 처리됩니다.
        try {
            timeoutPublisher.publishTimeout(reservationId, seatId);
        } catch (Exception e) {
            log.error("[Event] ⚠️ RabbitMQ 타임아웃 메시지 발행 실패 — 좌석 PENDING 고착 위험. " +
                    "reservationId={}, seatId={}. 수동 복구 또는 스케줄러 정리가 필요합니다.", reservationId, seatId, e);
        }
        // Kafka(결제 개시)는 사용자가 '내 예약'에서 결제 버튼을 눌렀을 때 POST /reservations/pay 에서 발행합니다.
    }
}
