package com.firstspring.reservation.reservation.service;

import com.firstspring.reservation.reservation.config.KafkaConfig;
import com.firstspring.reservation.reservation.dto.ReservationPaymentEvent;
import com.firstspring.reservation.reservation.dto.ReservationResponse;
import com.firstspring.reservation.reservation.dto.ReservationSuccessEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * [Kafka] 예약 관련 이벤트 발행 서비스 (Producer)
 *
 * 두 개의 토픽으로 역할을 분리합니다:
 * - reservation.success : 알림 이벤트 (CVC 미포함) → ReservationNotificationConsumer
 * - reservation.payment : 결제 요청 이벤트 (CVC 포함) → payment 서비스 전용
 *
 * Key 설계:
 * Kafka 메시지 Key = reservationId (String)
 * → 같은 reservationId의 이벤트는 항상 같은 파티션에 들어가 순서가 보장됩니다.
 */
@Slf4j
@Service
public class ReservationEventPublisher {

    private final KafkaTemplate<String, ReservationSuccessEvent> reservationKafkaTemplate;
    private final KafkaTemplate<String, ReservationPaymentEvent> reservationPaymentKafkaTemplate;

    public ReservationEventPublisher(
            @Qualifier("reservationKafkaTemplate") KafkaTemplate<String, ReservationSuccessEvent> reservationKafkaTemplate,
            @Qualifier("reservationPaymentKafkaTemplate") KafkaTemplate<String, ReservationPaymentEvent> reservationPaymentKafkaTemplate) {
        this.reservationKafkaTemplate = reservationKafkaTemplate;
        this.reservationPaymentKafkaTemplate = reservationPaymentKafkaTemplate;
    }

    /**
     * 알림 이벤트를 reservation.success 토픽으로 발행합니다. (CVC 미포함)
     * ReservationNotificationConsumer가 수신하여 이메일/SMS/푸시 알림을 처리합니다.
     */
    public void publishNotification(ReservationResponse response) {
        ReservationSuccessEvent event = new ReservationSuccessEvent(
                response.id(),
                response.userId(),
                response.seatId(),
                response.seatNumber(),
                response.tier(),
                response.matchTitle(),
                response.status().name(),
                response.createdAt());

        reservationKafkaTemplate.send(
                KafkaConfig.RESERVATION_SUCCESS_TOPIC,
                String.valueOf(response.id()),
                event);

        log.info("[Kafka] 알림 이벤트 발행 완료. reservationId={}, userId={}",
                response.id(), response.userId());
    }

    /**
     * 결제 요청 이벤트를 reservation.payment 토픽으로 발행합니다. (CVC 포함)
     * payment 서비스의 ReservationEventConsumer만 구독합니다 (최소 권한 원칙).
     *
     * ⚠ PCI-DSS: CVC는 이 이벤트에만 포함되며, payment 서비스에서 처리 후 영속 저장하지 않습니다.
     */
    public void publishPaymentRequest(ReservationResponse response, String cvc) {
        ReservationPaymentEvent event = new ReservationPaymentEvent(
                response.id(),
                response.userId(),
                response.seatId(),
                response.seatNumber(),
                response.tier(),
                response.matchTitle(),
                response.status().name(),
                cvc,
                response.createdAt());

        reservationPaymentKafkaTemplate.send(
                KafkaConfig.RESERVATION_PAYMENT_TOPIC,
                String.valueOf(response.id()),
                event);

        log.info("[Kafka] 결제 요청 이벤트 발행 완료. reservationId={}, userId={}",
                response.id(), response.userId());
    }
}
