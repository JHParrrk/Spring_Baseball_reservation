package com.firstspring.reservation.reservation.service;

import com.firstspring.reservation.common.exception.custom.InvalidRequestException;
import com.firstspring.reservation.common.exception.custom.ResourceNotFoundException;
import com.firstspring.reservation.reservation.dto.ReservationDto;
import com.firstspring.reservation.reservation.dto.ReservationResponse;
import com.firstspring.reservation.reservation.entity.Reservation;
import com.firstspring.reservation.reservation.event.ReservationCreatedEvent;
import com.firstspring.reservation.reservation.repository.ReservationRepository;
import com.firstspring.reservation.seat.entity.Seat;
import com.firstspring.reservation.seat.repository.SeatRepository;
import com.firstspring.reservation.user.entity.User;
import com.firstspring.reservation.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * [동시성 제어] 실제 DB 트랜잭션을 담당하는 서비스입니다.
 *
 * RedissonReservationService에서 분리한 이유:
 * - Spring의 @Transactional은 프록시(Proxy) 기반 AOP로 동작합니다.
 * - 같은 클래스 내에서 this.메서드()로 호출하면 프록시를 거치지 않아 @Transactional이 무시됩니다.
 * - 이를 'Self-invocation 문제'라고 합니다.
 * - 해결책: 트랜잭션이 필요한 로직을 별도 Bean으로 분리하여 Spring이 반드시 프록시를 통해 호출하도록 합니다.
 *
 * 실행 흐름:
 * RedissonReservationService (락 획득)
 * → ReservationExecutorService (DB 작업 + 트랜잭션 커밋 + RabbitMQ 메시지 발행)
 * → 트랜잭션 커밋 완료 후 RedissonReservationService에서 락 해제
 * 이 순서가 중요합니다: 락 해제 전에 트랜잭션이 반드시 커밋되어야 합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationExecutorService {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 실제 예약을 처리하는 트랜잭션 메서드입니다.
     * 이 메서드가 정상 종료되면 트랜잭션이 커밋됩니다.
     * 예외가 발생하면 자동으로 롤백됩니다.
     */
    @Transactional
    public List<ReservationResponse> execute(Long userId, ReservationDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        List<ReservationResponse> results = new ArrayList<>();

        for (Long seatId : dto.seatIds()) {
            // 1. 좌석 조회
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("좌석을 찾을 수 없습니다. ID: " + seatId));

            // 2. 좌석 상태 재확인 (락 진입 후 DB에서 최신 상태를 다시 확인하는 것이 안전합니다)
            if (seat.getStatus() != Seat.Status.AVAILABLE) {
                throw new InvalidRequestException("이미 선택된 좌석입니다. seatId=" + seatId);
            }

            // 3. 좌석 상태를 PENDING으로 변경
            seat.setStatus(Seat.Status.PENDING);
            try {
                seatRepository.save(seat);
            } catch (ObjectOptimisticLockingFailureException e) {
                log.warn("[예약] 낙관적 락 충돌 감지. seatId={}. 재시도 필요.", seatId);
                throw new InvalidRequestException("해당 좌석이 다른 요청에 의해 변경되었습니다. 다시 시도해주세요.");
            }

            // 4. 예약 레코드 생성
            Reservation reservation = new Reservation(user, seat);
            ReservationResponse response = ReservationResponse.from(reservationRepository.save(reservation));

            // 5. DB 커밋 성공 후 RabbitMQ 타임아웃 메시지 발행
            // 결제는 사용자가 '내 예약'에서 직접 개시합니다.
            // @TransactionalEventListener(AFTER_COMMIT) 리스너가 커밋 완료 후에만 실행합니다.
            applicationEventPublisher.publishEvent(new ReservationCreatedEvent(response, seat.getId()));

            results.add(response);
        }

        return results;
    }
}
