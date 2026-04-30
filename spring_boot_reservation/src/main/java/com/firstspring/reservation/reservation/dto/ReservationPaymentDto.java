package com.firstspring.reservation.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 결제 요청 DTO.
 *
 * POST /reservations/pay 에서 사용합니다.
 * 사용자가 '내 예약'에서 PENDING 상태의 예약들을 선택하고 CVC를 입력하면
 * 각 예약에 대해 Kafka 결제 이벤트가 발행됩니다.
 *
 * 예) POST /reservations/pay Body: { "reservationIds": [1, 2], "cvc": "456" }
 */
public record ReservationPaymentDto(
        @NotNull(message = "예약 목록은 필수 입력값입니다.")
        @Size(min = 1, max = 10, message = "1건 이상 최대 10건까지 결제 가능합니다.")
        List<@NotNull Long> reservationIds,

        @NotBlank(message = "CVC는 필수 입력값입니다.")
        String cvc) {
}
