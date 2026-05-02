package com.firstspring.reservation.match.dto;

import com.firstspring.reservation.match.service.MatchScheduleLogStore;

import java.time.LocalDateTime;

public record MatchScheduleLogResponse(
        LocalDateTime ranAt,
        String zone,
        long daysBefore,
        int openedCount,
        int closedCount,
        boolean success,
        String message) {

    public static MatchScheduleLogResponse from(MatchScheduleLogStore.MatchScheduleLog log) {
        return new MatchScheduleLogResponse(
                log.ranAt(),
                log.zone(),
                log.daysBefore(),
                log.openedCount(),
                log.closedCount(),
                log.success(),
                log.message());
    }
}
