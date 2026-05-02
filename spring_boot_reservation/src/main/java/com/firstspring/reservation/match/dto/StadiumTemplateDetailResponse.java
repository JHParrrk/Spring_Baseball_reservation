package com.firstspring.reservation.match.dto;

import com.firstspring.reservation.match.entity.StadiumSeatTemplate;

import java.math.BigDecimal;
import java.util.List;

public record StadiumTemplateDetailResponse(
        String stadiumName,
        int seatCount,
        List<SeatTemplateItem> seats) {

    public record SeatTemplateItem(
            String seatNumber,
            String tier,
            BigDecimal price) {
        public static SeatTemplateItem from(StadiumSeatTemplate seat) {
            return new SeatTemplateItem(seat.getSeatNumber(), seat.getTier(), seat.getPrice());
        }
    }

    public static StadiumTemplateDetailResponse from(String stadiumName, List<StadiumSeatTemplate> seats) {
        List<SeatTemplateItem> items = seats.stream()
                .map(SeatTemplateItem::from)
                .toList();
        return new StadiumTemplateDetailResponse(stadiumName, items.size(), items);
    }
}
