package com.firstspring.reservation.reservation.controller;

import com.firstspring.reservation.common.exception.custom.InvalidRequestException;
import com.firstspring.reservation.common.exception.custom.ResourceNotFoundException;
import com.firstspring.reservation.reservation.dto.ReservationResponse;
import com.firstspring.reservation.reservation.entity.Reservation;
import com.firstspring.reservation.reservation.repository.ReservationRepository;
import com.firstspring.reservation.seat.entity.Seat;
import com.firstspring.reservation.seat.repository.SeatRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/reservations")
@Tag(name = "Admin - Reservation API", description = "전체 예약 조회 및 강제 취소 (ADMIN 전용)")
public class AdminReservationController {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;

    public AdminReservationController(ReservationRepository reservationRepository, SeatRepository seatRepository) {
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "전체 예약 조회 (필터)", description = "상태(status), 유저(userId), 경기(matchId)로 필터링하여 예약 목록을 조회합니다. 파라미터 생략 시 전체 조회됩니다. (page/size/sort 파라미터로 페이지네이션)")
    public ResponseEntity<Page<ReservationResponse>> getReservations(
            @RequestParam(required = false) Reservation.Status status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long matchId,
            @PageableDefault(size = 20) Pageable pageable) {
        String statusStr = status != null ? status.name() : null;
        Page<ReservationResponse> result = reservationRepository
                .findAllByFilter(statusStr, userId, matchId, pageable)
                .map(ReservationResponse::from);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/cancel")
    @Transactional
    @Operation(summary = "예약 강제 취소", description = "관리자 권한으로 특정 예약을 강제 취소합니다. 좌석 상태가 AVAILABLE로 복원됩니다.")
    public ResponseEntity<ReservationResponse> forceCancel(@PathVariable Long id) {
        Reservation reservation = reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("예약 내역을 찾을 수 없습니다. ID: " + id));
        if (reservation.getStatus() == Reservation.Status.CANCELLED) {
            throw new InvalidRequestException("이미 취소된 예약입니다. reservationId=" + id);
        }
        reservation.setStatus(Reservation.Status.CANCELLED);
        reservationRepository.save(reservation);
        Seat seat = reservation.getSeat();
        seat.setStatus(Seat.Status.AVAILABLE);
        seatRepository.save(seat);
        log.info("[Admin] 예약 강제 취소 - reservationId={}, seatId={}", id, seat.getId());
        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }
}
