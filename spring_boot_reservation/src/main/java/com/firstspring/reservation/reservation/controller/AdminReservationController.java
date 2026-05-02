package com.firstspring.reservation.reservation.controller;

import com.firstspring.reservation.reservation.dto.ReservationResponse;
import com.firstspring.reservation.reservation.entity.Reservation;
import com.firstspring.reservation.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/reservations")
@Tag(name = "Admin - Reservation API", description = "전체 예약 조회 및 강제 취소 (ADMIN 전용)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    @Operation(summary = "전체 예약 조회 (필터)", description = "상태(status), 유저(userId), 경기(matchId)로 필터링하여 예약 목록을 조회합니다. 파라미터 생략 시 전체 조회됩니다. (page/size/sort 파라미터로 페이지네이션)")
    public ResponseEntity<Page<ReservationResponse>> getReservations(
            @RequestParam(required = false) Reservation.Status status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long matchId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reservationService.getReservationsForAdmin(status, userId, matchId, pageable));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "예약 강제 취소", description = "관리자 권한으로 특정 예약을 강제 취소합니다. 좌석 상태가 AVAILABLE로 복원됩니다.")
    public ResponseEntity<ReservationResponse> forceCancel(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.forceCancel(id));
    }
}
