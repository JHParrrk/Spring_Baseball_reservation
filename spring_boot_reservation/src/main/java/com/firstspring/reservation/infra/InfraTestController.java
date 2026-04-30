package com.firstspring.reservation.infra;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * [스프링 입문] Redis / RabbitMQ / Kafka 연동 테스트용 API 컨트롤러입니다.
 *
 * @ConditionalOnProperty : application.properties에서
 *                        app.infra-test.enabled=true 인 경우에만 이 Bean이 활성화됩니다.
 *                        프로덕션 환경에서는 이 값을 설정하지 않아 자동으로 비활성화됩니다.
 *
 * @RestController : JSON 응답을 네이티브로 지원하는 REST 컨트롤러 Bean
 *                 @RequestMapping("/api/infra-test") : 모든 엔드포인트의 기본 경로
 * @Tag : Swagger UI에서 이 컨트롤러를 그룹화하는 데 사용
 */
@ConditionalOnProperty(name = "app.infra-test.enabled", havingValue = "true")
@RestController
@RequestMapping("/api/infra-test")
@Tag(name = "Infra Integration API", description = "Redis, RabbitMQ, Kafka 데이터 입출력 테스트 기능")
public class InfraTestController {

    private final InfraTestService infraTestService;

    public InfraTestController(InfraTestService infraTestService) {
        this.infraTestService = infraTestService;
    }

    // ================== [1] Redis 테스트 ==================
    @PostMapping("/redis")
    @Operation(summary = "[Redis] Key-Value 저장", description = "Redis에 키-값 쌍을 저장합니다.")
    public ResponseEntity<String> saveRedis(@RequestParam String key, @RequestParam String value) {
        infraTestService.saveToRedis(key, value);
        return ResponseEntity.ok("Redis에 무사히 저장되었습니다! key: " + key + ", value: " + value);
    }

    @GetMapping("/redis")
    @Operation(summary = "[Redis] 값 조회", description = "입력한 key로 Redis에서 값을 꺼내옵니다.")
    public ResponseEntity<String> getRedis(@RequestParam String key) {
        String value = infraTestService.getFromRedis(key);
        return ResponseEntity.ok(value != null ? value : "값이 존재하지 않습니다.");
    }

    // ================== [2] RabbitMQ 테스트 ==================
    @PostMapping("/rabbitmq")
    @Operation(summary = "[RabbitMQ] 메시지 큐 전송", description = "메시지를 큐로 던집니다. (Consumer가 즉시 받아 변수에 보관합니다)")
    public ResponseEntity<String> sendRabbit(@RequestParam String message) {
        infraTestService.sendToRabbit(message);
        return ResponseEntity.ok("RabbitMQ 전송 성공 (명령창 로그에서 확인해보세요!) : " + message);
    }

    @GetMapping("/rabbitmq")
    @Operation(summary = "[RabbitMQ] 마지막으로 받은 메시지 조회", description = "RabbitMQ Consumer가 가장 마지막으로 받았던 메시지를 보여줍니다.")
    public ResponseEntity<String> receiveRabbit() {
        return ResponseEntity.ok(infraTestService.getLastRabbitMessage());
    }

    // ================== [3] Kafka 테스트 ==================
    @PostMapping("/kafka")
    @Operation(summary = "[Kafka] 토픽(Topic) 전송", description = "메시지를 카프카 test-topic 으로 던집니다.")
    public ResponseEntity<String> sendKafka(@RequestParam String message) {
        infraTestService.sendToKafka(message);
        return ResponseEntity.ok("Kafka 전송 성공 (명령창 로그에서 확인해보세요!) : " + message);
    }

    @GetMapping("/kafka")
    @Operation(summary = "[Kafka] 마지막으로 받은 메시지 조회", description = "Kafka Listener가 가장 마지막으로 받았던 토픽 메시지를 보여줍니다.")
    public ResponseEntity<String> receiveKafka() {
        return ResponseEntity.ok(infraTestService.getLastKafkaMessage());
    }
}