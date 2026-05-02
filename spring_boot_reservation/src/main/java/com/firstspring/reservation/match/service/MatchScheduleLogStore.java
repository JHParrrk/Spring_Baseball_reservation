package com.firstspring.reservation.match.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Component
public class MatchScheduleLogStore {

    @Value("${app.match.auto-open.log-retention:200}")
    private int retention;

    private final Deque<MatchScheduleLog> logs = new ArrayDeque<>();

    public synchronized void add(MatchScheduleLog log) {
        logs.addFirst(log);
        while (logs.size() > Math.max(1, retention)) {
            logs.removeLast();
        }
    }

    public synchronized List<MatchScheduleLog> getRecent(int limit) {
        int safeLimit = Math.max(1, limit);
        List<MatchScheduleLog> result = new ArrayList<>(Math.min(safeLimit, logs.size()));
        int count = 0;
        for (MatchScheduleLog log : logs) {
            if (count++ >= safeLimit) {
                break;
            }
            result.add(log);
        }
        return result;
    }

    public record MatchScheduleLog(
            LocalDateTime ranAt,
            String zone,
            long daysBefore,
            int openedCount,
            int closedCount,
            boolean success,
            String message) {
    }
}
