package com.firstspring.reservation.infra;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [인프라 통합 테스트] Redis, RabbitMQ, Kafka 연동 검증
 *
 * 실행 조건: 192.168.0.10 서버의 Redis, RabbitMQ, Kafka가 구동 중이어야 합니다.
 *
 * 테스트 전략:
 * - Redis : 저장(SET) 후 조회(GET), 동기 방식이므로 즉시 검증합니다.
 * - RabbitMQ: @RabbitListener가 별도 스레드에서 비동기 수신하므로
 * Awaitility로 최대 10초 대기 후 검증합니다.
 * - Kafka : @KafkaListener가 별도 스레드에서 비동기 수신하므로
 * Awaitility로 최대 15초 대기 후 검증합니다.
 * (Kafka는 RabbitMQ보다 Consumer Poll 주기가 길어 여유 있게 설정합니다.)
 *
 * 메시지 유니크성:
 * - UUID를 섞은 고유 메시지를 사용해 이전 테스트의 잔류 메시지와 구분합니다.
 */
@SpringBootTest
@ActiveProfiles("dev")
class InfraIntegrationTest {

    @Autowired
    private InfraTestService infraTestService;

    // ===================== [1] Redis =====================

    @Test
    @DisplayName("[Redis] 키-값 저장 후 동일한 값으로 조회되어야 한다")
    void redis_save_and_get() {
        // given
        String key = "test:infra:" + UUID.randomUUID();
        String value = "redis-value-" + UUID.randomUUID();

        // when
        infraTestService.saveToRedis(key, value);
        String result = infraTestService.getFromRedis(key);

        // then
        assertThat(result)
                .as("Redis에 저장한 값과 조회한 값이 일치해야 합니다.")
                .isEqualTo(value);
    }

    @Test
    @DisplayName("[Redis] 존재하지 않는 키 조회 시 null을 반환해야 한다")
    void redis_get_nonexistent_key_returns_null() {
        // given
        String nonExistentKey = "test:infra:nonexistent:" + UUID.randomUUID();

        // when
        String result = infraTestService.getFromRedis(nonExistentKey);

        // then
        assertThat(result)
                .as("존재하지 않는 키는 null을 반환해야 합니다.")
                .isNull();
    }

    // ===================== [2] RabbitMQ =====================

    @Test
    @DisplayName("[RabbitMQ] 메시지 발행 후 컨슈머가 동일한 메시지를 수신해야 한다")
    void rabbitMQ_send_and_receive() {
        // given: UUID를 사용해 이전 테스트 잔류 메시지와 구분
        String message = "rabbit-test-" + UUID.randomUUID();

        // when
        infraTestService.sendToRabbit(message);

        // then: @RabbitListener는 별도 스레드에서 비동기 실행 → Awaitility로 폴링 대기
        Awaitility.await()
                .alias("RabbitMQ Consumer 수신 대기")
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> assertThat(infraTestService.getLastRabbitMessage())
                        .as("RabbitMQ를 통해 수신된 메시지가 발행한 메시지와 일치해야 합니다.")
                        .isEqualTo(message));
    }

    // ===================== [3] Kafka =====================

    @Test
    @DisplayName("[Kafka] 메시지 발행 후 컨슈머가 동일한 메시지를 수신해야 한다")
    void kafka_send_and_receive() {
        // given: UUID를 사용해 이전 테스트 잔류 메시지와 구분
        String message = "kafka-test-" + UUID.randomUUID();

        // when
        infraTestService.sendToKafka(message);

        // then: @KafkaListener는 별도 스레드에서 비동기 실행 → Awaitility로 폴링 대기
        // Kafka Consumer Poll 주기(기본 500ms)를 고려해 RabbitMQ보다 넉넉하게 설정
        Awaitility.await()
                .alias("Kafka Consumer 수신 대기")
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> assertThat(infraTestService.getLastKafkaMessage())
                        .as("Kafka를 통해 수신된 메시지가 발행한 메시지와 일치해야 합니다.")
                        .isEqualTo(message));
    }
}
