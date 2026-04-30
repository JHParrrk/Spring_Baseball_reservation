package com.firstspring.reservation.match.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record MatchCreateRequest(
        @NotBlank(message = "경기 제목은 필수입니다.") String title,
        @NotNull(message = "경기 일시는 필수입니다.") @FutureOrPresent(message = "경기 일시는 현재 또는 미래 시간이어야 합니다.") LocalDateTime matchDate,
        @NotBlank(message = "경기장 이름은 필수입니다.") String stadiumName) {
}
