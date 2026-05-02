package com.firstspring.reservation.match.entity;

import com.firstspring.reservation.common.entity.BaseTimeEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "stadium_seat_templates", uniqueConstraints = @UniqueConstraint(name = "uk_stadium_template_seat", columnNames = {
        "stadium_name", "seat_number" }))
public class StadiumSeatTemplate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stadium_name", nullable = false, length = 50)
    private String stadiumName;

    @Column(name = "seat_number", nullable = false, length = 20)
    private String seatNumber;

    @Column(nullable = false, length = 20)
    private String tier;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    public StadiumSeatTemplate() {
    }

    public StadiumSeatTemplate(String stadiumName, String seatNumber, String tier, BigDecimal price) {
        this.stadiumName = stadiumName;
        this.seatNumber = seatNumber;
        this.tier = tier;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public String getStadiumName() {
        return stadiumName;
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
}
