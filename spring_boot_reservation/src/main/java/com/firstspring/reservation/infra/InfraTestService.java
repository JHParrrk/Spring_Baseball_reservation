package com.firstspring.reservation.infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

/**
 * [스프링 입문] Redis, RabbitMQ, Kafka 연동을 테스트하는 서비스입니다.
 *
 * @ConditionalOnProperty : app.infra-test.enabled=true 일 때만 Bean으로 등록됩니다.
 * @Service : 비즈니스 로직을 담당하는 서비스 Bean으로 등록됩니다.
 *
 *          외부 인프라 연동에 필요한 컨포넌트들은 생성자 주입으로 받습니다:
 *          - StringRedisTemplate : Redis key-value 스토어 연동
 *          - RabbitTemplate : RabbitMQ 메시지 발행
 *          - KafkaTemplate : Kafka 토픽 메시지 전송
 *
 *          @RabbitListener, @KafkaListener : 메시지가 도착하면 자동으로 호출되는 수신자 메서드입니다.
 */
@ConditionalOnProperty(name = "app.infra-test.enabled", havingValue = "true")
@Service
public class InfraTestService {

    private static final Logger logger = LoggerFactory.getLogger(InfraTestService.class);

    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // 수신된 가장 최근 메시지를 화면에 보여주기 위해 저장해두는 변수
    private final AtomicReference<String> lastRabbitMessage = new AtomicReference<>("수신된 메시지 없음");
    private final AtomicReference<String> lastKafkaMessage = new AtomicReference<>("수신된 메시지 없음");

    public InfraTestService(StringRedisTemplate redisTemplate, RabbitTemplate rabbitTemplate,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    // 1. Redis (Key-Value 데이터 저장)
    public void saveToRedis(String key, String value) {
        redisTemplate.opsForValue().set(key, value); // Redis SET 명령
        logger.info("[Redis] Saved: key={}, value={}", key, value);
    }

    public String getFromRedis(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 2. RabbitMQ (메시지 발행 및 수신)
    public void sendToRabbit(String message) {
        rabbitTemplate.convertAndSend("test.queue", message); // test.queue 큐로 메시지 발행
        logger.info("[RabbitMQ] Sent: {}", message);
    }

    // @RabbitListener : test.queue 로 메시지가 들어오면 자동 호출됩니다
    @RabbitListener(queuesToDeclare = @Queue("test.queue"))
    public void receiveFromRabbit(String message) {
        logger.info("[RabbitMQ] Received: {}", message);
        lastRabbitMessage.set(message);
    }

    public String getLastRabbitMessage() {
        return lastRabbitMessage.get();
    }

    // 3. Kafka (메시지 전송 및 수신)
    public void sendToKafka(String message) {
        kafkaTemplate.send("test-topic", message); // test-topic 토픽으로 메시지 전송
        logger.info("[Kafka] Sent: {}", message);
    }

    // @KafkaListener : test-topic 에 메시지가 들어오면 자동 호출됩니다
    @KafkaListener(topics = "test-topic", groupId = "test-group")
    public void receiveFromKafka(String message) {
        logger.info("[Kafka] Received: {}", message);
        lastKafkaMessage.set(message);
    }

    public String getLastKafkaMessage() {
        return lastKafkaMessage.get();
    }
}