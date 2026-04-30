package com.firstspring.reservation.reservation;

import com.firstspring.reservation.match.entity.MatchInfo;
import com.firstspring.reservation.match.repository.MatchRepository;
import com.firstspring.reservation.reservation.dto.ReservationDto;
import com.firstspring.reservation.reservation.repository.ReservationRepository;
import com.firstspring.reservation.reservation.service.RedissonReservationService;
import com.firstspring.reservation.seat.entity.Seat;
import com.firstspring.reservation.seat.repository.SeatRepository;
import com.firstspring.reservation.user.entity.User;
import com.firstspring.reservation.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [동시성 테스트] Redisson 분산 락을 이용한 좌석 예약 동시성 제어 검증 테스트
 *
 * 시나리오:
 * - 100개의 스레드가 동시에 같은 좌석(seatId)에 예약 요청을 보냅니다.
 * - 결과적으로 단 1명만 성공하고, 나머지 99명은 실패해야 합니다.
 *
 * 핵심 도구:
 * - ExecutorService: 스레드 풀 생성
 * - CountDownLatch: 모든 스레드가 동시에 출발하도록 동기화
 * - AtomicInteger: 멀티스레드 환경에서 안전하게 카운팅
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.format_sql=false"
})
class ConcurrencyReservationTest {

    @Autowired
    private RedissonReservationService redissonReservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    private Long testSeatId;
    private Long testUserId;
    private Long testMatchId;

    @BeforeEach
    void setUp() {
        // 1. 유저 생성 (User 엔티티의 실제 생성자 사용)
        User user = new User("테스트유저", "test@test.com");
        testUserId = userRepository.save(user).getId();

        // 2. 경기 정보 생성 (Seat은 MatchInfo 없이는 DB 저장 불가 - nullable=false)
        MatchInfo match = new MatchInfo(
                "테스트 경기",
                LocalDateTime.now().plusDays(7),
                "잠실구장");
        MatchInfo savedMatch = matchRepository.save(match);
        testMatchId = savedMatch.getId();

        // 3. 좌석 생성 (Seat 생성자: MatchInfo, seatNumber, tier, price)
        Seat seat = new Seat(savedMatch, "A1", "VIP", new BigDecimal("150000"));
        testSeatId = seatRepository.save(seat).getId();
    }

    @AfterEach
    void tearDown() {
        // 이 테스트에서 생성한 데이터만 정확히 삭제 (기존 더미 데이터 보호)
        // FK 제약조건 순서: reservations → seats → matches → users
        reservationRepository.findAll().stream()
                .filter(r -> testSeatId.equals(r.getSeat().getId()))
                .forEach(reservationRepository::delete);
        seatRepository.deleteById(testSeatId);
        matchRepository.deleteById(testMatchId);
        userRepository.deleteById(testUserId);
    }

    @Test
    @DisplayName("100명이 동시에 같은 좌석을 예약할 때 단 1명만 성공해야 한다")
    void testConcurrencyWithRedisson() throws InterruptedException {
        int threadCount = 100;
        // 스레드풀 크기 32: 한 번에 최대 32개 스레드가 동시 실행됨
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        // latch: threadCount개의 스레드가 모두 준비될 때까지 대기시키는 신호기
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ReservationDto dto = new ReservationDto(List.of(testSeatId));

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    redissonReservationService.createReservationWithLock(testUserId, dto);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    // 작업 완료 시 카운트다운
                    latch.countDown();
                }
            });
        }

        // 100개 스레드 전부 완료될 때까지 메인 스레드 대기
        latch.await();
        executorService.shutdown();

        // --- 결과 출력 ---
        System.out.println("===== 동시성 테스트 결과 =====");
        System.out.println("총 요청 수  : " + threadCount);
        System.out.println("성공 횟수   : " + successCount.get());
        System.out.println("실패 횟수   : " + failCount.get());

        Seat finalSeat = seatRepository.findById(testSeatId).orElseThrow();
        System.out.println("최종 좌석 상태: " + finalSeat.getStatus());
        System.out.println("최종 버전 번호: " + finalSeat.getVersion());
        System.out.println("=========================");

        // --- 검증 ---
        // 1. 성공은 반드시 1번이어야 한다
        assertThat(successCount.get()).isEqualTo(1);
        // 2. 나머지 99개는 모두 실패해야 한다
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
        // 3. 좌석 상태가 PENDING으로 바뀌었어야 한다
        assertThat(finalSeat.getStatus()).isEqualTo(Seat.Status.PENDING);
    }
}
