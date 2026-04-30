package com.firstspring.reservation.reservation.service;

import com.firstspring.reservation.reservation.config.KafkaConfig;
import com.firstspring.reservation.reservation.dto.PaymentResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * [Kafka] payment.result 토픽 Consumer
 *
 * payment 서비스가 발행한 결제 결과 이벤트를 수신합니다.
 * - SUCCESS : 예약 상태를 CONFIRMED로 업데이트
 * - FAILED  : 예약 상태를 CANCELLED로 업데이트하고 좌석을 AVAILABLE로 복원
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentResultConsumer {

    private final ReservationService reservationService;

    @KafkaListener(
            topics = KafkaConfig.PAYMENT_RESULT_TOPIC,
            groupId = KafkaConfig.PAYMENT_RESULT_GROUP_ID,
            containerFactory = "paymentResultListenerContainerFactory")
    public void handlePaymentResult(PaymentResultEvent event) {
        log.info("[Kafka] 결제 결과 수신. reservationId={}, status={}, paymentId={}",
                event.reservationId(), event.status(), event.paymentId());
        try {
            if ("SUCCESS".equals(event.status())) {
                reservationService.confirmReservation(event.reservationId());
            } else {
                log.warn("[Kafka] 결제 실패 — 예약 취소 처리. reservationId={}, reason={}",
                        event.reservationId(), event.failureReason());
                reservationService.cancelReservationByPaymentFailure(event.reservationId());
            }
        } catch (Exception e) {
            // 예외 발생 시 KafkaConfig의 DefaultErrorHandler가 3회 재시도.
            // 재시도 후에도 실패하면 이 예외가 전파되어 메시지가 드롭됩니다.
            // 운영 환경에서는 Dead Letter Topic(DLT) 연결을 권장합니다.
            log.error("[Kafka] 결제 결과 처리 중 예외 발생. reservationId={}, status={}. 예약 상태 불일치 가능성 있음.",
                    event.reservationId(), event.status(), e);
            throw e;
        }
    }
}
