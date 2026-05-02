package com.firstspring.reservation.reservation.controller;

import com.firstspring.reservation.auth.jwt.UserPrincipal;
import com.firstspring.reservation.reservation.dto.ReservationDto;
import com.firstspring.reservation.reservation.dto.ReservationPaymentDto;
import com.firstspring.reservation.reservation.dto.ReservationResponse;
import com.firstspring.reservation.reservation.service.RedissonReservationService;
import com.firstspring.reservation.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * [스프링 입문] 예약 API 컨트롤러입니다.
 *
 * @RestController : REST API 컨트롤러로 등록합니다. 응답이 JSON으로 직렬화됩니다.
 *                 @RequestMapping("/reservations") : 모든 매핑 메서드의 기본 URL 경로를
 *                 /reservations로 설정합니다.
 * @Tag : Swagger UI에서 이 컨트롤러를 그룹화할 제목과 설명입니다.
 *
 *      보안 햵심:
 * @AuthenticationPrincipal UserPrincipal principal
 *                          -> JwtAuthenticationFilter가 SecurityContext에 담아둔
 *                          UserPrincipal을 주입받습니다.
 *                          -> URL 파라미터로 userId를 받지 않습니다. 이를 통해 타인의 데이터를 목할 수
 *                          없습니다.
 */
@RestController
@RequestMapping("/reservations")
@Tag(name = "Reservation API", description = "예약 생성/조회/취소/삭제 관련 API")
public class ReservationController {

    private final ReservationService reservationService;
    private final RedissonReservationService redissonReservationService;

    public ReservationController(ReservationService reservationService,
            RedissonReservationService redissonReservationService) {
        this.reservationService = reservationService;
        this.redissonReservationService = redissonReservationService;
    }

    @PostMapping
    @Operation(summary = "예약 생성", description = "JWT 토큰으로 인증된 사용자가 최대 10매 좌석을 예약합니다. (분산 락 + 타임아웃/알림 이벤트 적용)")
    public ResponseEntity<List<ReservationResponse>> createReservation(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReservationDto dto) {
        return ResponseEntity.status(201)
                .body(redissonReservationService.createReservationWithLock(principal.getUserId(), dto));
    }

    @GetMapping("/me")
    @Operation(summary = "내 예약 조회", description = "JWT 토큰으로 인증된 본인의 예약 내역을 페이지네이션으로 조회합니다. (기본: 20개)")
    public ResponseEntity<Page<ReservationResponse>> getMyReservations(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reservationService.getReservationsByUser(principal.getUserId(), pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "단일 예약 조회", description = "예약 ID로 본인의 특정 예약 상세 정보를 조회합니다.")
    public ResponseEntity<ReservationResponse> getReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(reservationService.getReservation(id, principal.getUserId()));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "예약 취소", description = "본인의 예약을 취소합니다. 좌석 상태가 AVAILABLE로 복원됩니다.")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(reservationService.cancelReservation(id, principal.getUserId()));
    }

    @PostMapping("/pay")
    @Operation(summary = "예약 결제 개시", description = "PENDING 상태의 예약을 선택하여 CVC와 함께 결제를 요청합니다. 각 예약에 대해 Kafka 결제 이벤트가 발행됩니다.")
    public ResponseEntity<List<ReservationResponse>> payReservations(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReservationPaymentDto dto) {
        return ResponseEntity.ok(
                reservationService.payReservations(dto.reservationIds(), principal.getUserId(), dto.cvc()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "예약 삭제", description = "본인의 취소된(CANCELLED) 예약 레코드를 완전히 삭제합니다.")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        reservationService.deleteReservation(id, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
