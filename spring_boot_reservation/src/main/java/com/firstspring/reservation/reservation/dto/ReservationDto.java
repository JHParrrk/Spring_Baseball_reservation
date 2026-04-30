package com.firstspring.reservation.reservation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * [스프링 입문] 예약 생성 요청 데이터 객체(DTO)입니다.
 *
 * Java 16+ record : 불변(immutable) 데이터 클래스.
 * 생성자, getter, equals, hashCode, toString을 자동 생성합니다.
 * → 요청 본문에서 JSON → Java 객체로 자동 역직렬화됩니다.
 *
 * 예) POST /reservations Body: { "seatIds": [1, 2, 3] }
 * → ReservationDto(seatIds=[1, 2, 3]) 객체로 컨트롤러에 들어옴
 * 최대 10매까지 한 번에 예약 가능합니다. 결제는 '내 예약'에서 별도로 진행합니다.
 */
public record ReservationDto(
        @NotNull(message = "좌석 목록은 필수 입력값입니다.")
        @Size(min = 1, max = 10, message = "1매 이상 최대 10매까지 예약 가능합니다.")
        List<@NotNull Long> seatIds) {
}
