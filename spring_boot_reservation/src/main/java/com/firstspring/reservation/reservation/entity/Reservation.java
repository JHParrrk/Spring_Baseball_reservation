package com.firstspring.reservation.reservation.entity;

import com.firstspring.reservation.common.entity.BaseTimeEntity;
import com.firstspring.reservation.seat.entity.Seat;
import com.firstspring.reservation.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * [스프링 입문] 예약(Reservation) 엔티티입니다.
 *
 * DB의 reservations 테이블과 매핑됩니다.
 * User와 Seat를 연결하는 중간 테이블 엔티티입니다.
 *
 * Soft Delete 적용:
 * 
 * @SQLDelete : delete 쿼리가 실행될 때 대신 update is_deleted = true를 실행합니다.
 * @Where : 모든 조회 쿼리에 is_deleted = false 조건을 자동으로 추가합니다.
 */
@Entity
@Table(name = "reservations")
@SQLDelete(sql = "UPDATE reservations SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    /** 예약 상태 enum. PENDING=예매중, CONFIRMED=확정, CANCELLED=취소 */
    public enum Status {
        PENDING, CONFIRMED, CANCELLED
    }

    public Reservation() {
    }

    public Reservation(User user, Seat seat) {
        this.user = user;
        this.seat = seat;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Seat getSeat() {
        return seat;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
