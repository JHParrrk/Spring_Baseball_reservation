package com.firstspring.reservation.config;

import com.firstspring.reservation.match.entity.MatchInfo;
import com.firstspring.reservation.match.repository.MatchRepository;
import com.firstspring.reservation.seat.entity.Seat;
import com.firstspring.reservation.seat.repository.SeatRepository;
import com.firstspring.reservation.user.entity.User;
import com.firstspring.reservation.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * [스프링 입문] 애플리케이션 기동 완료 시 자동으로 실행되는 리스너입니다.
 *
 * @Component : 이 클래스를 스프링 Bean으로 등록합니다.
 *            → @Service / @Repository / @Controller 와 달리 특별한 역할 구분 없이 범용적 Bean에
 *            젯합니다.
 *
 *            ApplicationListener<ApplicationReadyEvent> 인터페이스를 구현하면
 *            스프링이 완전히 시작된 직후(ApplicationReadyEvent) onApplicationEvent()를
 *            호출합니다.
 *
 *            여기서는 두 가지 작업을 수행합니다:
 *            1) Swagger URL 로깅 : 개발자가 화면에서 쉽게 확인할 수 있도록
 *            2) 더미 데이터 생성 : DB가 비어 있으면 테스트용 데이터를 자동 입력
 */
@Component
public class AppStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(AppStartupListener.class);

    private final Environment environment;
    private final MatchRepository matchRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    public AppStartupListener(Environment environment,
            MatchRepository matchRepository,
            SeatRepository seatRepository,
            UserRepository userRepository) {
        this.environment = environment;
        this.matchRepository = matchRepository;
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
    }

    /**
     * [스프링 입문] @Transactional 어노테이션
     * 이 메서드 안에서 일어나는 모든 DB 작업이 하나의 트랜잭션으로 묶입니다.
     * 도중에 오류가 나면 전체가 롤백(Rollback)됩니다.
     */
    @Override
    @Transactional
    public void onApplicationEvent(@org.springframework.lang.NonNull ApplicationReadyEvent event) {
        logSwaggerUrl();
        initDummyData();
    }

    private void logSwaggerUrl() {
        String port = environment.getProperty("local.server.port");
        if (port == null) {
            port = environment.getProperty("server.port", "8080");
        }
        String swaggerUrl = String.format("http://localhost:%s/swagger-ui/index.html", port);
        logger.info("====================================================================");
        logger.info("Application is running!");
        logger.info("Swagger UI: {}", swaggerUrl);
        logger.info("====================================================================");
    }

    private void initDummyData() {
        if (matchRepository.count() > 0) {
            logger.info("이미 경기 데이터가 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        logger.info("=== 더미 데이터 초기화 시작 ===");

        // 1. 테스트 사용자 10명 생성
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String email = "test" + i + "@example.com";
            if (userRepository.findByEmail(email).isEmpty()) {
                users.add(new User("tester" + i, email));
            }
        }
        if (!users.isEmpty()) {
            userRepository.saveAll(users);
        }
        logger.info("테스트 사용자 10명 생성 완료");

        // 2. 경기 생성
        MatchInfo match = new MatchInfo(
                "LG 트윈스 vs KIA 타이거즈",
                LocalDateTime.now().plusDays(7),
                "잠실야구장");
        match = matchRepository.saveAndFlush(match);
        logger.info("경기 생성 완료: {}", match.getTitle());

        // 3. 좌석 1,000개 생성 (A~D 구역, 각 250석)
        String[] zones = { "A", "B", "C", "D" };
        List<Seat> seats = new ArrayList<>();
        for (String zone : zones) {
            String tier = (zone.equals("A") || zone.equals("B")) ? "VIP" : "NORMAL";
            BigDecimal price = tier.equals("VIP") ? new BigDecimal("35000") : new BigDecimal("15000");
            for (int num = 1; num <= 250; num++) {
                Seat seat = new Seat(match, zone + "구역-" + num, tier, price);
                seats.add(seat);
            }
        }
        seatRepository.saveAll(seats);
        logger.info("해당 경기의 총 {}개 좌석 더미 데이터 생성 완료!", seats.size());
        logger.info("=== 더미 데이터 초기화 프로세스 완료 ===");
    }
}
