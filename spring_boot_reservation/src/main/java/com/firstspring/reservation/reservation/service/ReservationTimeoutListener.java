package com.firstspring.reservation.reservation.service;

import com.firstspring.reservation.reservation.config.RabbitMQConfig;
import com.firstspring.reservation.reservation.dto.ReservationTimeoutMessage;
import com.firstspring.reservation.reservation.entity.Reservation;
import com.firstspring.reservation.reservation.repository.ReservationRepository;
import com.firstspring.reservation.seat.entity.Seat;
import com.firstspring.reservation.seat.repository.SeatRepository;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;

/**
 * [RabbitMQ] 예약 타임아웃 메시지 수신 및 자동 취소 처리 Listener
 *
 * reservation.cancel.queue 를 구독합니다.
 * pending.queue에서 TTL(5분) 만료 후 DLX를 거쳐 이 큐로 메시지가 도착합니다.
 *
 * 처리 로직:
 * 1) reservationId로 예약을 조회합니다.
 * 2) 예약 상태가 여전히 PENDING이면 → CANCELLED로 변경합니다.
 * (만약 그 사이에 사용자가 결제(CONFIRMED)했으면 아무 작업도 하지 않습니다.)
 * 3) 좌석 상태를 다시 AVAILABLE로 복원합니다.
 */
@Slf4j
@Service
public class ReservationTimeoutListener {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    /**
     * TransactionTemplate: 프로그래매틱 트랜잭션 제어.
     * 
     * @Transactional + basicAck 순서 보장이 불가능하기 때문에
     *                TransactionTemplate.executeWithoutResult() 로 트랜잭션 범위를 명시적으로 닫은
     *                뒤
     *                basicAck 를 호출합니다. (DB 커밋 → ack 순서 보장)
     */
    private final TransactionTemplate transactionTemplate;

    public ReservationTimeoutListener(ReservationRepository reservationRepository,
            SeatRepository seatRepository,
            PlatformTransactionManager transactionManager) {
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @RabbitListener(queues = RabbitMQConfig.CANCEL_QUEUE, containerFactory = "manualAckListenerContainerFactory")
    public void handleTimeout(ReservationTimeoutMessage message, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        Long reservationId = message.reservationId();
        Long seatId = message.seatId();

        log.info("[RabbitMQ] 타임아웃 메시지 수신. reservationId={}, seatId={}", reservationId, seatId);

        try {
            // TransactionTemplate: executeWithoutResult() 가 리턴되는 시점에 트랜잭션이 커밋됩니다.
            // basicAck는 반드시 그 이후에 호출되어야 합니다.
            // (기존 @Transactional 방식은 메서드 리턴 후 프록시가 커밋하므로
            // 메서드 내부의 basicAck가 커밋보다 먼저 실행되는 문제가 있었습니다.)
            transactionTemplate.executeWithoutResult(status -> {
                // [C1] 비관적 락: Kafka payment.result와 동시에 도달하는 Race Condition 방지
                // findByIdForUpdate(SELECT FOR UPDATE)로 confirmReservation과 동일한 락을 경쟁합니다.
                reservationRepository.findByIdForUpdate(reservationId).ifPresentOrElse(reservation -> {

                    // 2. 여전히 PENDING 상태인 경우에만 취소 처리
                    if (reservation.getStatus() == Reservation.Status.PENDING) {
                        reservation.setStatus(Reservation.Status.CANCELLED);
                        reservationRepository.save(reservation);

                        // 3. 좌석 상태를 AVAILABLE로 복원
                        // [P2] 비관적 락으로 조회하여 Kafka CONFIRMED 처리와의 Race Condition 방지
                        seatRepository.findByIdForUpdate(seatId).ifPresent(seat -> {
                            if (seat.getStatus() == Seat.Status.PENDING) {
                                seat.setStatus(Seat.Status.AVAILABLE);
                                seatRepository.save(seat);
                                log.info("[RabbitMQ] 좌석 상태 복원 완료. seatId={} → AVAILABLE", seatId);
                            }
                        });

                        log.info("[RabbitMQ] 예약 자동 취소 완료. reservationId={} → CANCELLED", reservationId);

                    } else {
                        // CONFIRMED 또는 이미 CANCELLED인 경우 - 아무 작업 안 함
                        log.info("[RabbitMQ] 이미 처리된 예약. reservationId={}, 현재 상태={}",
                                reservationId, reservation.getStatus());
                    }

                }, () -> log.warn("[RabbitMQ] 예약을 찾을 수 없음 (이미 삭제됨). reservationId={}", reservationId));
            });

            // 트랜잭션 커밋 완료 후 메시지 정상 소비 확인 (DB 커밋 → ack 순서 보장)
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("[RabbitMQ] 메시지 처리 중 예외 발생. reservationId={}", reservationId, e);
            // requeue=false: 동일 메시지 무한 재처리 루프 방지 (필요 시 DLQ 연결 가능)
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
