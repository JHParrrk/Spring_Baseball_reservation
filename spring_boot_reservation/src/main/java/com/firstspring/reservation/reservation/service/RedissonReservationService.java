package com.firstspring.reservation.reservation.service;

import com.firstspring.reservation.common.exception.custom.InvalidRequestException;
import com.firstspring.reservation.reservation.dto.ReservationDto;
import com.firstspring.reservation.reservation.dto.ReservationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * [동시성 제어] Redisson 분산 락을 이용한 좌석 예약 진입점 서비스입니다.
 *
 * 역할: 락 획득 → 트랜잭션 실행(ReservationExecutorService 위임) → 락 해제
 *
 * 핵심 원리 (Redis 분산 락):
 * - Redis에 "lock:seat:{seatId}" 라는 키를 생성합니다.
 * - 가장 먼저 이 키를 점유한 1명만 이후 로직을 진행합니다.
 * - 나머지 요청은 waitTime 동안 대기하다 포기하거나, tryLock 즉시 실패를 반환합니다.
 *
 * 중요: 이 클래스는 @Transactional을 붙이지 않습니다.
 * 이유: 락 해제(finally) 전에 트랜잭션 커밋이 완료되어야 하는데,
 * 
 * @Transactional을 여기 붙이면 이 메서드가 종료될 때(= 락 해제 후)에 커밋되어
 *                 락 해제와 커밋 사이에 다른 스레드가 진입할 수 있는 틈이 생깁니다.
 *                 따라서 트랜잭션은 별도 Bean(ReservationExecutorService)에서 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedissonReservationService {

    private final RedissonClient redissonClient;

    // Self-invocation 방지를 위해 트랜잭션 로직을 별도 Bean으로 분리하여 주입합니다.
    private final ReservationExecutorService reservationExecutorService;

    /**
     * 좌석 예약 메서드 (분산 락 적용)
     *
     * @param userId 예약 요청 유저 ID
     * @param dto    예약 요청 DTO (seatId 포함)
     * @return 예약 결과 DTO
     *
     *         waitTime (5초): 락 획득을 위해 대기하는 최대 시간. 5초 내에 못 잡으면 포기합니다.
     *         leaseTime (10초): 락을 점유하는 최대 시간. 10초가 지나면 자동으로 락이 해제됩니다.
     *         (서버가 죽어도 영구적으로 락이 잠기는 상황을 방지)
     *         주의: leaseTime은 반드시 waitTime보다 커야 합니다.
     *         DB 트랜잭션 처리 시간(수 초)을 고려하여 충분한 여유를 두어야 합니다.
     */
    public List<ReservationResponse> createReservationWithLock(Long userId, ReservationDto dto) {
        // [Q3] 중복 seatId 방어: 같은 좌석에 대한 다중 예약 레코드 생성 방지
        long distinctCount = dto.seatIds().stream().distinct().count();
        if (distinctCount != dto.seatIds().size()) {
            throw new InvalidRequestException("중복된 좌석 ID가 포함되어 있습니다.");
        }
        // 데드락 방지: seatId 오름차순 정렬로 항상 같은 순서로 락 획득
        List<Long> sortedSeatIds = dto.seatIds().stream().sorted().toList();
        List<RLock> acquiredLocks = new ArrayList<>();

        try {
            for (Long seatId : sortedSeatIds) {
                RLock lock = redissonClient.getLock("lock:seat:" + seatId);
                boolean acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);

                if (!acquired) {
                    log.warn("[분산락] 락 획득 실패 - 다른 요청이 점유 중. seatId={}, userId={}",
                            seatId, userId);
                    throw new InvalidRequestException(
                            "현재 다른 사용자가 해당 좌석(" + seatId + ")을 예약 중입니다. 잠시 후 다시 시도해주세요.");
                }

                acquiredLocks.add(lock);
                log.info("[분산락] 락 획득 성공. seatId={}, userId={}", seatId, userId);
            }

            return reservationExecutorService.execute(userId, dto);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("예약 처리 중 인터럽트가 발생했습니다.");
        } finally {
            for (RLock lock : acquiredLocks) {
                try {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                } catch (Exception e) {
                    log.error("[분산락] 락 해제 실패 — leaseTime 만료 대기 필요.", e);
                }
            }
        }
    }
}
