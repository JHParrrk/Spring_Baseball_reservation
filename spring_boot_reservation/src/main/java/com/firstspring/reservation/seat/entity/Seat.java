package com.firstspring.reservation.seat.entity;

import com.firstspring.reservation.common.entity.BaseTimeEntity;
import com.firstspring.reservation.match.entity.MatchInfo;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * [스프링 입문] 좌석(Seat) 엔티티입니다.
 *
 * @Entity, @Table : DB의 seats 테이블과 매핑됩니다.
 * 
 * @UniqueConstraint : 한 경기에 동일한 좌석번호가 중복으로 등록되지 않도록 DB 레벨에서 방지합니다.
 *
 *                   연관 관계:
 *                   Seat -N:1- MatchInfo
 *                   한 경기(MatchInfo)에 여러 좌석(Seat)이 연결됩니다.
 *
 * @ManyToOne(fetch = FetchType.LAZY):
 *                  좌석을 조회할 때마다 MatchInfo를 JOIN하지 않고,
 *                  실제로 getMatch()을 호출할 때만 쿼리합니다. (지연 로딩 = 성능 최적화)
 *
 * @Version : 낙관적 락(Optimistic Lock)입니다.
 *          동시에 두 명이 같은 좌석을 예매하려 할 때 충돌을 감지합니다.
 */
@Entity
@Table(name = "seats", uniqueConstraints = @UniqueConstraint(name = "uk_match_seat", columnNames = { "match_id",
        "seat_number" }))
public class Seat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private MatchInfo match;

    @Column(name = "seat_number", nullable = false, length = 20)
    private String seatNumber;

    @Column(nullable = false, length = 20)
    private String tier;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // @Enumerated(EnumType.STRING) : enum을 DB에 "AVAILABLE"이라는 문자열로 저장합니다.
    // EnumType.ORDINAL은 0, 1, 2 숫자로 저장하는데, 코드 변경 시 버그가 생길 수 있어 STRING을 권장
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.AVAILABLE;

    // @Version : JPA 낙관적 락. 동시 수정 시 원래 버전과 다르면 OptimisticLockException 발생
    @Version
    @Column(nullable = false)
    private Long version = 0L;

    /**
     * 좌석 상태를 나타내는 enum입니다.
     * AVAILABLE = 예매 가능
     * PENDING = 예매 진행 중 (5분 타임아웃 대기)
     * RESERVED = 예매 확정 (결제 성공 시 confirmReservation()에서 설정)
     */
    public enum Status {
        AVAILABLE, PENDING, RESERVED
    }

    public Seat() {
    }

    public Seat(MatchInfo match, String seatNumber, String tier, BigDecimal price) {
        this.match = match;
        this.seatNumber = seatNumber;
        this.tier = tier;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public MatchInfo getMatch() {
        return match;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public String getTier() {
        return tier;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Status getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
