package com.firstspring.reservation.match.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record StadiumTemplateCreateRequest(
        @NotBlank(message = "구장 이름은 필수입니다.") String stadiumName,
        @NotEmpty(message = "좌석 템플릿은 최소 1개 이상이어야 합니다.") @Valid List<SeatTemplateItem> seats) {

    public record SeatTemplateItem(
            @NotBlank(message = "좌석 번호는 필수입니다.") String seatNumber,
            @NotBlank(message = "좌석 등급은 필수입니다.") String tier,
            @NotNull(message = "좌석 가격은 필수입니다.") @DecimalMin(value = "0.01", message = "좌석 가격은 0보다 커야 합니다.") BigDecimal price) {
    }
}
