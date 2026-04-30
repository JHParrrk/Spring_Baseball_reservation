package com.firstspring.reservation.seat.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record SeatBulkCreateRequest(
        @NotEmpty(message = "좌석 목록은 1개 이상이어야 합니다.") @Valid List<SeatItem> seats) {

    public record SeatItem(
            @NotBlank(message = "좌석 번호는 필수입니다.") String seatNumber,
            @NotBlank(message = "등급은 필수입니다.") String tier,
            @NotNull(message = "가격은 필수입니다.") @DecimalMin(value = "0.0", inclusive = false, message = "가격은 0보다 커야 합니다.") BigDecimal price) {
    }
}
