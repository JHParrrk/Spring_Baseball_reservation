package com.firstspring.payment.consumer;

import com.firstspring.payment.config.KafkaConfig;
import com.firstspring.payment.dto.ReservationSuccessEvent;
import com.firstspring.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * [Kafka] reservation.success 토픽 Consumer
 *
 * reservation 서비스가 예약 생성 완료 후 발행한 이벤트를 수신합니다.
 * 수신 후 PaymentService.processPayment()를 호출하여 결제를 처리합니다.
 *
 * 처리 흐름:
 * reservation.success (Kafka) → [이 Consumer] → PaymentService (mock 결제)
 *   → payment.result (Kafka) → reservation 서비스 (상태 업데이트)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = KafkaConfig.RESERVATION_PAYMENT_TOPIC,
            groupId = KafkaConfig.PAYMENT_GROUP_ID,
            containerFactory = "reservationEventListenerContainerFactory")
    public void handleReservationSuccess(ReservationSuccessEvent event) {
        log.info("[Kafka] 예약 성공 이벤트 수신. reservationId={}, userId={}, 좌석={}, 경기='{}'",
                event.reservationId(), event.userId(), event.seatNumber(), event.matchTitle());
        paymentService.processPayment(event);
    }
}
