package com.firstspring.reservation.reservation.service;

import com.firstspring.reservation.reservation.config.RabbitMQConfig;
import com.firstspring.reservation.reservation.dto.ReservationTimeoutMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * [RabbitMQ] 예약 타임아웃 메시지 발행 서비스
 *
 * 예약 생성 직후 호출되어, 5분 지연 취소 메시지를 RabbitMQ에 발행합니다.
 *
 * 발행 흐름:
 * 이 서비스 → reservation.pending.exchange → reservation.pending.queue (TTL 5분)
 * ↓ (5분 뒤 자동 만료)
 * reservation.dlx
 * ↓
 * reservation.cancel.queue
 * ↓
 * ReservationTimeoutListener (자동 취소)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationTimeoutPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 예약 생성 후 호출 - 5분 뒤 자동 취소를 위한 메시지를 큐에 발행합니다.
     *
     * @param reservationId 취소 대상 예약 ID
     * @param seatId        좌석 AVAILABLE 복원을 위한 좌석 ID
     */
    public void publishTimeout(Long reservationId, Long seatId) {
        ReservationTimeoutMessage message = new ReservationTimeoutMessage(reservationId, seatId);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PENDING_EXCHANGE,
                RabbitMQConfig.PENDING_ROUTING_KEY,
                message);

        log.info("[RabbitMQ] 타임아웃 메시지 발행 완료. reservationId={}, seatId={} (5분 후 자동 취소 예정)",
                reservationId, seatId);
    }
}
