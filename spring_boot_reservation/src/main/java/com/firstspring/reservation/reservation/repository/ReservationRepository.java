package com.firstspring.reservation.reservation.repository;

import com.firstspring.reservation.reservation.dto.UserReservationStat;
import com.firstspring.reservation.reservation.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * [스프링 입문] Reservation 엔티티에 대한 JPA 레포지터리입니다.
 *
 * N+1 문제 방지:
 * - Reservation → Seat → Match 체인으로 Lazy 로딩이 발생하면 N개의 추가 쿼리가 발생합니다.
 * - JOIN FETCH로 한 번의 쿼리에 모든 연관 엔티티를 함께 로딩합니다.
 *
 * 페이지네이션:
 * - 전체 조회 시 Pageable을 받아 Page 객체로 반환합니다.
 * - 대용량 데이터에서 메모리 오버플로우를 방지합니다.
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * 전체 예약 목록을 Seat, Match를 JOIN FETCH하여 페이지네이션으로 조회합니다.
     * N+1 문제를 방지합니다.
     * countQuery를 별도 명시하여 Hibernate JOIN FETCH + count 혼용 경고를 방지합니다.
     */
    @Query(value = "SELECT r FROM Reservation r JOIN FETCH r.seat s JOIN FETCH s.match", countQuery = "SELECT COUNT(r) FROM Reservation r JOIN r.seat s JOIN s.match")
    Page<Reservation> findAllWithDetails(Pageable pageable);

    /**
     * 특정 사용자의 예약 목록을 Seat, Match를 JOIN FETCH하여 조회합니다.
     */
    @Query("SELECT r FROM Reservation r JOIN FETCH r.seat s JOIN FETCH s.match WHERE r.user.id = :userId")
    List<Reservation> findByUserIdWithDetails(@Param("userId") Long userId);

    /**
     * 특정 사용자의 예약 목록을 Seat, Match를 JOIN FETCH하여 페이지네이션으로 조회합니다.
     */
    @Query(value = "SELECT r FROM Reservation r JOIN FETCH r.seat s JOIN FETCH s.match WHERE r.user.id = :userId", countQuery = "SELECT COUNT(r) FROM Reservation r JOIN r.seat s WHERE r.user.id = :userId")
    Page<Reservation> findByUserIdWithDetails(@Param("userId") Long userId, Pageable pageable);

    /**
     * 단일 예약을 Seat, Match를 JOIN FETCH하여 조회합니다.
     */
    @Query("SELECT r FROM Reservation r JOIN FETCH r.seat s JOIN FETCH s.match WHERE r.id = :id")
    Optional<Reservation> findByIdWithDetails(@Param("id") Long id);

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findBySeatId(Long seatId);

    /**
     * 여러 예약 ID를 한 번의 쿼리로 조회합니다 (Seat, Match JOIN FETCH 포함).
     * payReservations()에서 N+1 방지를 위해 사용합니다.
     */
    @Query("SELECT r FROM Reservation r JOIN FETCH r.user u JOIN FETCH r.seat s JOIN FETCH s.match WHERE r.id IN :ids")
    List<Reservation> findAllByIdInWithDetails(@Param("ids") List<Long> ids);

    /**
     * 여러 유저의 예약 통계를 단 1번의 쿼리로 가져옵니다.
     * 생성자 표현식으로 UserReservationStat 타입을 직접 반환해 Object[] 캐스팅을 제거합니다.
     * Admin 유저 목록 조회 시 N+1 방지에 사용합니다.
     */
    @Query("SELECT new com.firstspring.reservation.reservation.dto.UserReservationStat(r.user.id, r.status, COUNT(r)) FROM Reservation r WHERE r.user.id IN :userIds GROUP BY r.user.id, r.status")
    List<UserReservationStat> countGroupByUserAndStatus(@Param("userIds") List<Long> userIds);

    /** Admin 필터링 조회: null 조건은 무시합니다. 페이지네이션으로 OOM을 방지합니다. */
    @Query(value = """
            SELECT r FROM Reservation r
            JOIN FETCH r.user u
            JOIN FETCH r.seat s
            JOIN FETCH s.match m
            WHERE (:statusStr IS NULL OR cast(r.status as string) = :statusStr)
              AND (:userId IS NULL OR u.id = :userId)
              AND (:matchId IS NULL OR m.id = :matchId)
            """, countQuery = """
            SELECT COUNT(r) FROM Reservation r
            JOIN r.user u
            JOIN r.seat s
            JOIN s.match m
            WHERE (:statusStr IS NULL OR cast(r.status as string) = :statusStr)
              AND (:userId IS NULL OR u.id = :userId)
              AND (:matchId IS NULL OR m.id = :matchId)
            """)
    Page<Reservation> findAllByFilter(
            @Param("statusStr") String statusStr,
            @Param("userId") Long userId,
            @Param("matchId") Long matchId,
            Pageable pageable);
}
