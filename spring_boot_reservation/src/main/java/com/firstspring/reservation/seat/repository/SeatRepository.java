package com.firstspring.reservation.seat.repository;

import com.firstspring.reservation.seat.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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

    /**
     * [P2] 비관적 쓰기 락(SELECT FOR UPDATE)으로 좌석을 조회합니다.
     * ReservationTimeoutListener에서 Kafka confirmReservation과의 Race Condition 방지에 사용합니다.
     * 반드시 @Transactional 메서드 내에서만 호출하세요.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :id")
    Optional<Seat> findByIdForUpdate(@Param("id") Long id);
}
