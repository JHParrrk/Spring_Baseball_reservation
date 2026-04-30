package com.firstspring.reservation.seat.repository;

import com.firstspring.reservation.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * [스프링 입문] Seat 엔티티에 대한 JPA 레포지터리입니다.
 *
 * 메서드 이름 규칙으로 Spring Data JPA가 SQL을 자동 생성합니다:
 * - findByMatchId(matchId)
 * -> SELECT * FROM seats WHERE match_id = ?
 * - findByMatchIdAndStatus(matchId, status)
 * -> SELECT * FROM seats WHERE match_id = ? AND status = ?
 */
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByMatchId(Long matchId);

    List<Seat> findByMatchIdAndStatus(Long matchId, Seat.Status status);

    List<Seat> findByMatchIdAndSeatNumberIn(Long matchId, List<String> seatNumbers);
}
