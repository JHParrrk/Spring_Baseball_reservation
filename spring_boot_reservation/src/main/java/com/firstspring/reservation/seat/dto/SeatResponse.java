package com.firstspring.reservation.seat.dto;

import com.firstspring.reservation.seat.entity.Seat;

import java.math.BigDecimal;

public record SeatResponse(
        Long id,
        String seatNumber,
        String tier,
        BigDecimal price,
        Seat.Status status) {

    public static SeatResponse from(Seat s) {
        return new SeatResponse(s.getId(), s.getSeatNumber(), s.getTier(), s.getPrice(), s.getStatus());
    }
}
