package com.firstspring.reservation.reservation.dto;

import com.firstspring.reservation.reservation.entity.Reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * [스프링 입문] 예약 응답 데이터 객체(DTO)입니다.
 *
 * Java record 사용: Reservation 엔티티를 API 응답으로 내보낼 때 사용합니다.
 *
 * 엔티티를 직접 노옶하지 않고 DTO로 감슸는 이유:
 * - 필요한 정보만 선제적으로 노출
 * - 엔티티의 내부 구조(Lazy 연관 관계) 노출 시 의도치 않은 데이터 직렬화 방지
 *
 * from(Reservation r): 엔티티 → DTO 변환 정적 팩토리 메서드
 */
public record ReservationResponse(
        Long id,
        Long userId,
        Long seatId,
        String seatNumber,
        String tier,
        BigDecimal price,
        String matchTitle,
        LocalDateTime matchDate,
        String stadiumName,
        Reservation.Status status,
        LocalDateTime createdAt) {
    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getUser().getId(),
                r.getSeat().getId(),
                r.getSeat().getSeatNumber(),
                r.getSeat().getTier(),
                r.getSeat().getPrice(),
                r.getSeat().getMatch().getTitle(),
                r.getSeat().getMatch().getMatchDate(),
                r.getSeat().getMatch().getStadiumName(),
                r.getStatus(),
                r.getCreatedAt());
    }
}
