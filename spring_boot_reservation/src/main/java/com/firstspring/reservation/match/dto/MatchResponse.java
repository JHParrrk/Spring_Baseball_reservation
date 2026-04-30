package com.firstspring.reservation.match.dto;

import com.firstspring.reservation.match.entity.MatchInfo;

import java.time.LocalDateTime;

public record MatchResponse(
        Long id,
        String title,
        LocalDateTime matchDate,
        String stadiumName,
        MatchInfo.MatchStatus status) {

    public static MatchResponse from(MatchInfo m) {
        return new MatchResponse(m.getId(), m.getTitle(), m.getMatchDate(), m.getStadiumName(), m.getStatus());
    }
}
