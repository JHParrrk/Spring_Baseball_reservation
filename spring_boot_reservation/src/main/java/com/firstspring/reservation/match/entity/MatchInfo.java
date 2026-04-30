package com.firstspring.reservation.match.entity;

import com.firstspring.reservation.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
public class MatchInfo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version = 0L;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "match_date", nullable = false)
    private LocalDateTime matchDate;

    @Column(name = "stadium_name", nullable = false, length = 50)
    private String stadiumName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchStatus status = MatchStatus.UPCOMING;

    /** 경기 진행 상태 enum */
    public enum MatchStatus {
        UPCOMING, // 예매 오픈 전
        ON_SALE, // 예매 중
        CLOSED, // 예매 마감
        CANCELLED // 취소됨
    }

    public MatchInfo() {
    }

    public MatchInfo(String title, LocalDateTime matchDate, String stadiumName) {
        this.title = title;
        this.matchDate = matchDate;
        this.stadiumName = stadiumName;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getMatchDate() {
        return matchDate;
    }

    public String getStadiumName() {
        return stadiumName;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }
}
