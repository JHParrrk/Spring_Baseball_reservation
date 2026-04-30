package com.firstspring.reservation.reservation.service;

import com.firstspring.reservation.reservation.config.KafkaConfig;
import com.firstspring.reservation.reservation.dto.ReservationResponse;
import com.firstspring.reservation.reservation.dto.ReservationSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * [Kafka] 예약 성공 이벤트 발행 서비스 (Producer)
 *
 * 예약 트랜잭션이 성공한 직후 호출됩니다.
 * ReservationResponse를 ReservationSuccessEvent로 변환하여
 * 'reservation.success' 토픽으로 발행합니다.
 *
 * Key 설계:
 * Kafka 메시지 Key = reservationId (String)
 * → 같은 reservationId의 이벤트는 항상 같은 파티션에 들어가 순서가 보장됩니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationEventPublisher {

    @Qualifier("reservationKafkaTemplate")
    private final KafkaTemplate<String, ReservationSuccessEvent> reservationKafkaTemplate;

    /**
     * 예약 성공 이벤트를 Kafka 토픽으로 발행합니다.
     *
     * @param response 방금 생성된 예약 응답 DTO
     */
    public void publishSuccess(ReservationResponse response, String cvc) {
        // CVC는 /reservations/pay 에서 @NotBlank 검증 후 전달됩니다.
        ReservationSuccessEvent event = new ReservationSuccessEvent(
                response.id(),
                response.userId(),
                response.seatId(),
                response.seatNumber(),
                response.tier(),
                response.matchTitle(),
                response.status().name(),
                cvc,
                response.createdAt());

        reservationKafkaTemplate.send(
                KafkaConfig.RESERVATION_SUCCESS_TOPIC,
                String.valueOf(response.id()), // Kafka 메시지 Key = reservationId
                event);

        log.info("[Kafka] 예약 성공 이벤트 발행 완료. reservationId={}, userId={}, 좌석={}",
                response.id(), response.userId(), response.seatNumber());
    }
}
