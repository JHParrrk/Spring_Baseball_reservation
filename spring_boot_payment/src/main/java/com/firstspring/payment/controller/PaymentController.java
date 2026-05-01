package com.firstspring.payment.controller;

import com.firstspring.payment.auth.UserPrincipal;
import com.firstspring.payment.dto.PaymentResponse;
import com.firstspring.payment.repository.PaymentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Payment 조회 API
 * 결제 이력 확인 및 상태 조회용 엔드포인트입니다.
 */
@Tag(name = "Payment", description = "결제 조회 API")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;

    @Operation(summary = "전체 결제 내역 조회 (관리자 전용)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(paymentRepository.findAll(pageable).map(PaymentResponse::from));
    }

    @Operation(summary = "예약 ID로 결제 내역 조회 (본인 결제만)")
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<PaymentResponse> getByReservationId(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return paymentRepository.findByReservationId(reservationId)
                .filter(p -> p.getUserId().equals(principal.getUserId()))
                .map(PaymentResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "결제 ID로 결제 내역 조회 (본인 결제만)")
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getById(
            @PathVariable long paymentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return paymentRepository.findById(paymentId)
                .filter(p -> p.getUserId().equals(principal.getUserId()))
                .map(PaymentResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

