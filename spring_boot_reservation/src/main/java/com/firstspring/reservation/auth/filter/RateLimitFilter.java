package com.firstspring.reservation.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * [Rate Limiting] Redis(Redisson) 기반 IP별 요청 제한 필터입니다.
 *
 * 예약 경로(/reservations)에 대해 IP당 분당 60회로 제한합니다.
 * 초과 시 HTTP 429 Too Many Requests를 반환합니다.
 *
 * 구현 근거:
 * - 이미 프로젝트에 Redisson이 도입되어 있어 추가 의존성 없이 RRateLimiter를 활용합니다.
 * - X-Forwarded-For 헤더를 우선 확인하여 프록시 뒤 클라이언트 IP를 정확히 식별합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedissonClient redissonClient;

    /** IP당 분당 허용 요청 수 */
    private static final long RATE_PER_MINUTE = 60;

    /**
     * RateLimiter 초기화(trySetRate) 여부를 로컬에서 추적합니다.
     * 서버 재시작 시 Redis의 기존 limiter 상태는 유지되므로 trySetRate 중복 호출이 방지됩니다.
     * (Redis 재시작 시에는 자동으로 초기화가 다시 수행됩니다.)
     */
    private final ConcurrentMap<String, Boolean> initializedKeys = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 인증/예약 경로에만 Rate Limit 적용 (Swagger, 헬스체크 등은 제외)
        if (!path.startsWith("/reservations")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String key = "rate_limit:" + clientIp;

        // Fail-Open: Redis 장애 시 서비스를 중단하지 않고 요청을 통과시킵니다.
        // 가용성을 우선시하는 정책이며, Redis 복구 후 Rate Limit이 자동으로 재활성화됩니다.
        try {
            RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

            // 이미 초기화된 key는 trySetRate를 다시 호출하지 않습니다 (성능 최적화).
            // 초기화 기록이 없는 경우에만 Redis에 trySetRate를 호출합니다.
            initializedKeys.computeIfAbsent(key, k -> {
                rateLimiter.trySetRate(RateType.OVERALL, RATE_PER_MINUTE, 1, RateIntervalUnit.MINUTES);
                return Boolean.TRUE;
            });

            if (!rateLimiter.tryAcquire()) {
                log.warn("[RateLimit] 요청 제한 초과. IP={}, URI={}", clientIp, path);
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"error\":\"Too Many Requests\",\"message\":\"요청이 너무 많습니다. 잠시 후 다시 시도해주세요.\"}");
                return;
            }
        } catch (Exception e) {
            // Fail-Open: Redis 연결 오류 등 예외 발생 시 Rate Limit을 건너뜁니다.
            log.warn("[RateLimit] Redis 오류로 Rate Limit 건너뜀 (Fail-Open). IP={}, error={}", clientIp, e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * X-Forwarded-For 헤더를 우선 확인하여 실제 클라이언트 IP를 반환합니다.
     *
     * ⚠️ 보안 주의: X-Forwarded-For 헤더는 클라이언트가 위조할 수 있습니다.
     * 신뢰할 수 있는 프록시/로드밸런서(nginx, AWS ALB 등)를 통해서만 트래픽이 유입되는
     * 환경에서 이 헤더를 신뢰해야 합니다.
     * 직접 인터넷에 노출된 서버에서 이 헤더를 무조건 신뢰하면 IP 스푸핑으로
     * Rate Limit을 우회할 수 있습니다.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // 프록시 체인에서 첫 번째 IP가 실제 클라이언트 IP입니다
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
