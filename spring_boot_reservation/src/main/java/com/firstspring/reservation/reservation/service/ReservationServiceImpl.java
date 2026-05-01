package com.firstspring.reservation.reservation.service;

import com.firstspring.reservation.common.exception.custom.InvalidRequestException;
import com.firstspring.reservation.common.exception.custom.ResourceNotFoundException;
import com.firstspring.reservation.common.exception.custom.UnauthorizedAccessException;
import com.firstspring.reservation.reservation.dto.ReservationResponse;
import com.firstspring.reservation.reservation.entity.Reservation;
import com.firstspring.reservation.reservation.event.ReservationPaymentInitiatedEvent;
import com.firstspring.reservation.reservation.repository.ReservationRepository;
import com.firstspring.reservation.seat.entity.Seat;
import com.firstspring.reservation.seat.repository.SeatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * [스프링 입문] ReservationService 의 실제 구현 클래스입니다.
 *
 * @Service : 이 클래스가 비즈니스 로직을 담당하는 서비스 Bean임을 선언합니다.
 * @Transactional : 클래스 수준의 @Transactional은 모든 public 메서드에 적용됩니다.
 *                → 예시 도중 오류 발생 시 자동 롤백(Rollback)
 *
 *                판단 로직 핵심:
 *                1) getReservation : 소유자 확인 후 예약 조회
 *                2) cancelReservation : 소유자 확인 + 중복 취소 막기 → 취소 + 좌석 AVAILABLE
 *                복원 (명시적 save)
 *                3) deleteReservation : 소유자 확인 → 취소 안 된 상태면 좌석도 AVAILABLE 복원 →
 *                레코드 삭제
 */
@Slf4j
@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final ReservationEventPublisher eventPublisher;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
            SeatRepository seatRepository,
            ReservationEventPublisher eventPublisher,
            ApplicationEventPublisher applicationEventPublisher) {
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
        this.eventPublisher = eventPublisher;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    // Fetch Join으로 N+1 방지 + 페이지네이션으로 OOM 방지
    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservationsByUser(Long userId, Pageable pageable) {
        log.info("사용자 예약 조회 - userId={}", userId);
        return reservationRepository.findByUserIdWithDetails(userId, pageable)
                .map(ReservationResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservation(Long id, Long expectedUserId) {
        Reservation reservation = reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("예약 내역을 찾을 수 없습니다. ID: " + id));
        if (!reservation.getUser().getId().equals(expectedUserId)) {
            throw new UnauthorizedAccessException("해당 예약에 대한 조회 권한이 없습니다.");
        }
        return ReservationResponse.from(reservation);
    }

    @Override
    public ReservationResponse cancelReservation(Long id, Long expectedUserId) {
        log.info("예약 취소 요청 - reservationId={}, userId={}", id, expectedUserId);
        // [C1] 비관적 락: Kafka confirmReservation / RabbitMQ 타임아웃과 동시 상태 전이 Race Condition 방지
        Reservation reservation = reservationRepository.findByIdWithDetailsForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("예약 내역을 찾을 수 없습니다. ID: " + id));
        if (!reservation.getUser().getId().equals(expectedUserId)) {
            throw new UnauthorizedAccessException("해당 예약에 대한 취소 권한이 없습니다.");
        }
        if (reservation.getStatus() == Reservation.Status.CANCELLED) {
            throw new InvalidRequestException("이미 취소된 예약입니다.");
        }
        // CONFIRMED(결제 완료) 상태도 취소 허용.
        // 실제 결제 환불은 외부 PG 연동 시 여기에 환불 API 호출을 추가하세요.
        if (reservation.getStatus() == Reservation.Status.CONFIRMED) {
            log.info("결제 완료 예약 취소 처리 - reservationId={} (환불 필요)", id);
        }
        reservation.setStatus(Reservation.Status.CANCELLED);
        reservationRepository.save(reservation);
        // 명시적 save로 좌석 상태 AVAILABLE 복원을 안전하게 저장
        Seat seat = reservation.getSeat();
        seat.setStatus(Seat.Status.AVAILABLE);
        seatRepository.save(seat);
        log.info("예약 취소 완료 - reservationId={}, seatId={}", id, seat.getId());
        return ReservationResponse.from(reservation);
    }

    @Override
    public void deleteReservation(Long id, Long expectedUserId) {
        log.info("예약 삭제 요청 - reservationId={}, userId={}", id, expectedUserId);
        Reservation reservation = reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("예약 내역을 찾을 수 없습니다. ID: " + id));
        if (!reservation.getUser().getId().equals(expectedUserId)) {
            throw new UnauthorizedAccessException("해당 예약에 대한 삭제 권한이 없습니다.");
        }
        // [S1] 결제 완료 예약 삭제 방어: 환불 없이 데이터 소멸 방지
        if (reservation.getStatus() == Reservation.Status.CONFIRMED) {
            throw new InvalidRequestException("결제 완료된 예약은 삭제할 수 없습니다. 취소 요청 후 삭제하세요.");
        }
        // 취소 상태가 아닌 예약 삭제 시 좌석을 다시 AVAILABLE로 복원
        if (reservation.getStatus() != Reservation.Status.CANCELLED) {
            Seat seat = reservation.getSeat();
            seat.setStatus(Seat.Status.AVAILABLE);
            seatRepository.save(seat);
        }
        reservationRepository.deleteById(id);
        log.info("예약 삭제 완료 - reservationId={}", id);
    }

    @Override
    public void confirmReservation(Long reservationId) {
        log.info("[Payment] 결제 성공 수신 — 예약 CONFIRMED 처리. reservationId={}", reservationId);
        // [C1] 비관적 락: RabbitMQ 타임아웃과 동시 상태 전이 Race Condition 방지
        Reservation reservation = reservationRepository.findByIdWithDetailsForUpdate(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("예약 내역을 찾을 수 없습니다. ID: " + reservationId));

        if (reservation.getStatus() == Reservation.Status.CANCELLED) {
            log.warn("[Payment] 이미 취소된 예약에 CONFIRMED 불가 (RabbitMQ 타임아웃이 먼저 처리됨). reservationId={}", reservationId);
            return;
        }
        if (reservation.getStatus() == Reservation.Status.CONFIRMED) {
            log.warn("[Payment] 이미 CONFIRMED 상태. 중복 처리 무시. reservationId={}", reservationId);
            return;
        }
        reservation.setStatus(Reservation.Status.CONFIRMED);
        reservationRepository.save(reservation);

        // 좌석 상태를 PENDING → RESERVED (결제 확정)로 변경
        Seat seat = reservation.getSeat();
        seat.setStatus(Seat.Status.RESERVED);
        seatRepository.save(seat);

        log.info("[Payment] 예약 CONFIRMED 완료. reservationId={}, seatId={} → RESERVED", reservationId, seat.getId());
    }

    @Override
    public void cancelReservationByPaymentFailure(Long reservationId) {
        log.warn("[Payment] 결제 실패 수신 — 예약 CANCELLED 처리. reservationId={}", reservationId);
        // [C1] 비관적 락: RabbitMQ 타임아웃과 동시 상태 전이 Race Condition 방지
        Reservation reservation = reservationRepository.findByIdWithDetailsForUpdate(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("예약 내역을 찾을 수 없습니다. ID: " + reservationId));

        if (reservation.getStatus() == Reservation.Status.CANCELLED) {
            log.warn("[Payment] 이미 취소된 예약입니다. reservationId={}", reservationId);
            return;
        }
        reservation.setStatus(Reservation.Status.CANCELLED);
        reservationRepository.save(reservation);

        Seat seat = reservation.getSeat();
        seat.setStatus(Seat.Status.AVAILABLE);
        seatRepository.save(seat);
        log.info("[Payment] 결제 실패로 예약 취소 완료. reservationId={}, seatId={}", reservationId, seat.getId());
    }

    @Override
    public List<ReservationResponse> payReservations(List<Long> reservationIds, Long userId, String cvc) {
        log.info("[Pay] 결제 요청 — userId={}, reservationIds={}", userId, reservationIds);

        // 1단계: 전체 예약을 한 번의 쿼리로 조회 (N+1 방지)
        List<Reservation> reservations = reservationRepository.findAllByIdInWithDetails(reservationIds);

        // 조회 결과와 요청 수가 일치하는지 확인 (없는 ID가 섞여 있으면 거부)
        if (reservations.size() != reservationIds.size()) {
            throw new ResourceNotFoundException("일부 예약을 찾을 수 없습니다. 요청=" + reservationIds.size() + ", 조회=" + reservations.size());
        }

        // 2단계: 전체 예약 검증 (Kafka 발행 전에 모두 검증 — 중간 실패로 인한 부분 발행 방지)
        List<ReservationResponse> results = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (!reservation.getUser().getId().equals(userId)) {
                throw new UnauthorizedAccessException("해당 예약에 대한 결제 권한이 없습니다. reservationId=" + reservation.getId());
            }
            if (reservation.getStatus() != Reservation.Status.PENDING) {
                throw new InvalidRequestException("결제 가능한 상태(PENDING)가 아닙니다. reservationId=" + reservation.getId()
                        + ", 현재 상태: " + reservation.getStatus());
            }
            results.add(ReservationResponse.from(reservation));
        }

        // 3단계: 검증 통과 후 Spring 이벤트 발행 (모두 성공하거나 모두 안 하거나)
        // @TransactionalEventListener(AFTER_COMMIT)이 DB 커밋 완료 후에만 Kafka를 발행합니다.
        for (ReservationResponse response : results) {
            applicationEventPublisher.publishEvent(new ReservationPaymentInitiatedEvent(response, cvc));
            log.info("[Pay] 결제 개시 Spring 이벤트 발행 — reservationId={}", response.id());
        }

        return results;
    }

    @Override
    public ReservationResponse forceCancel(Long reservationId) {
        log.warn("[Admin] 예약 강제 취소 요청 — reservationId={}", reservationId);
        // [C1] 비관적 락: 사용자 cancelReservation / RabbitMQ 타임아웃과의 동시 상태 전이 Race Condition 방지
        Reservation reservation = reservationRepository.findByIdWithDetailsForUpdate(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("예약 내역을 찾을 수 없습니다. ID: " + reservationId));
        if (reservation.getStatus() == Reservation.Status.CANCELLED) {
            throw new InvalidRequestException("이미 취소된 예약입니다. reservationId=" + reservationId);
        }
        reservation.setStatus(Reservation.Status.CANCELLED);
        reservationRepository.save(reservation);
        Seat seat = reservation.getSeat();
        seat.setStatus(Seat.Status.AVAILABLE);
        seatRepository.save(seat);
        log.info("[Admin] 예약 강제 취소 완료 — reservationId={}, seatId={}", reservationId, seat.getId());
        return ReservationResponse.from(reservation);
    }
}
