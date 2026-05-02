package com.firstspring.reservation.match.service;

import com.firstspring.reservation.match.entity.MatchInfo;
import com.firstspring.reservation.match.repository.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class MatchAutoOpenScheduler {

    private static final Logger log = LoggerFactory.getLogger(MatchAutoOpenScheduler.class);

    private final MatchRepository matchRepository;
    private final MatchScheduleLogStore logStore;

    @Value("${app.match.auto-open.days-before:7}")
    private long daysBefore;

    @Value("${app.match.auto-open.zone:Asia/Seoul}")
    private String zone;

    public MatchAutoOpenScheduler(MatchRepository matchRepository, MatchScheduleLogStore logStore) {
        this.matchRepository = matchRepository;
        this.logStore = logStore;
    }

    @Scheduled(cron = "${app.match.auto-open.cron:0 */10 * * * *}", zone = "${app.match.auto-open.zone:Asia/Seoul}")
    @Transactional
    public void openUpcomingMatches() {
        ZoneId zoneId = ZoneId.of(zone);
        LocalDateTime now = LocalDateTime.now(zoneId);
        LocalDateTime openBoundary = now.plusDays(daysBefore);

        try {
            List<MatchInfo> openTargets = matchRepository.findByStatusAndMatchDateBetween(
                    MatchInfo.MatchStatus.UPCOMING,
                    now,
                    openBoundary);

            List<MatchInfo> closeTargets = matchRepository.findByStatusAndMatchDateBefore(
                    MatchInfo.MatchStatus.UPCOMING,
                    now);

            for (MatchInfo match : openTargets) {
                match.setStatus(MatchInfo.MatchStatus.ON_SALE);
            }
            for (MatchInfo match : closeTargets) {
                match.setStatus(MatchInfo.MatchStatus.CLOSED);
            }

            if (!openTargets.isEmpty()) {
                matchRepository.saveAll(openTargets);
            }
            if (!closeTargets.isEmpty()) {
                matchRepository.saveAll(closeTargets);
            }

            int openedCount = openTargets.size();
            int closedCount = closeTargets.size();
            String message = String.format("auto-open=%d, auto-close=%d", openedCount, closedCount);
            logStore.add(new MatchScheduleLogStore.MatchScheduleLog(
                    now,
                    zone,
                    daysBefore,
                    openedCount,
                    closedCount,
                    true,
                    message));

            log.info("[MatchSchedule] {} (window: {} ~ {}, zone: {})", message, now, openBoundary, zone);
        } catch (Exception e) {
            logStore.add(new MatchScheduleLogStore.MatchScheduleLog(
                    now,
                    zone,
                    daysBefore,
                    0,
                    0,
                    false,
                    e.getMessage()));
            throw e;
        }
    }
}
