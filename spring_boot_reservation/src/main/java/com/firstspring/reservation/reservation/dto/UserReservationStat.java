package com.firstspring.reservation.reservation.dto;

import com.firstspring.reservation.reservation.entity.Reservation;

/**
 * 유저별 예약 통계 조회 쿼리 결과를 타입 안전하게 담는 Projection DTO입니다.
 * countGroupByUserAndStatus JPQL 생성자 표현식과 함께 사용합니다.
 */
public record UserReservationStat(Long userId, Reservation.Status status, Long count) {
}
