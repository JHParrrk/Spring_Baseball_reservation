package com.firstspring.reservation.reservation.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * [RabbitMQ 설정] 예약 자동 취소를 위한 지연(TTL) 큐 구성
 *
 * 핵심 개념 - Dead Letter Exchange(DLX) 패턴:
 * 1) reservation.pending.queue 에 메시지를 발행합니다. (TTL: 5분)
 * 2) 메시지가 5분 동안 소비되지 않으면(TTL 만료) "죽은 편지"가 됩니다.
 * 3) 죽은 편지는 DLX(Dead Letter Exchange)를 통해
 * reservation.cancel.queue 로 자동 라우팅됩니다.
 * 4) ReservationTimeoutListener가 reservation.cancel.queue를 구독하여
 * 아직 PENDING인 예약을 CANCELLED 처리합니다.
 *
 * 큐 구조:
 * [발행] → pending.queue (TTL 5분)
 * ↓ (만료 시 DLX로 전달)
 * reservation.dlx
 * ↓
 * cancel.queue ← [Listener 구독]
 */
@Configuration
public class RabbitMQConfig {

    // --- 큐 이름 상수 ---
    public static final String PENDING_QUEUE = "reservation.pending.queue";
    public static final String CANCEL_QUEUE = "reservation.cancel.queue";
    public static final String CANCEL_DLQ = "reservation.cancel.dlq";

    // --- Exchange 이름 상수 ---
    public static final String PENDING_EXCHANGE = "reservation.pending.exchange";
    public static final String DLX_EXCHANGE = "reservation.dlx";
    public static final String CANCEL_DLX_EXCHANGE = "reservation.cancel.dlx";

    // --- Routing Key 상수 ---
    public static final String PENDING_ROUTING_KEY = "reservation.pending";
    public static final String CANCEL_ROUTING_KEY = "reservation.cancel";
    public static final String CANCEL_DLQ_ROUTING_KEY = "reservation.cancel.dead";

    /** TTL 5분 (밀리초) */
    private static final int TTL_MS = 5 * 60 * 1000;

    // ===================== Exchange 정의 =====================

    /** 메시지를 처음 발행할 Exchange */
    @Bean
    public DirectExchange pendingExchange() {
        return new DirectExchange(PENDING_EXCHANGE);
    }

    /** TTL 만료된 메시지를 받아 cancel.queue로 보내는 Dead Letter Exchange */
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    /**
     * cancel.queue에서 처리 실패(basicNack requeue=false)된 메시지를 받는 Exchange.
     * cancel.queue의 DLX로 설정되어, 처리 불가 메시지를 cancel.dlq로 라우팅합니다.
     */
    @Bean
    public DirectExchange cancelDlxExchange() {
        return new DirectExchange(CANCEL_DLX_EXCHANGE);
    }

    // ===================== Queue 정의 =====================

    /**
     * 예약 직후 메시지가 발행되는 큐 (TTL: 5분)
     * x-message-ttl : 큐 내 모든 메시지의 만료 시간 (5분)
     * x-dead-letter-exchange : TTL 만료 시 메시지를 보낼 Exchange
     * x-dead-letter-routing-key : DLX로 전달할 때 사용할 Routing Key
     */
    @Bean
    public Queue pendingQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", TTL_MS);
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", CANCEL_ROUTING_KEY);
        return new Queue(PENDING_QUEUE, true, false, false, args);
    }

    /** Listener가 구독하는 큐 (TTL 만료된 메시지가 도착하는 곳) */
    @Bean
    public Queue cancelQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", CANCEL_DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", CANCEL_DLQ_ROUTING_KEY);
        return new Queue(CANCEL_QUEUE, true, false, false, args);
    }

    /**
     * cancel.queue에서 처리 실패한 메시지를 보관하는 Dead Letter Queue.
     * 수동 복구 및 원인 분석용으로 활용합니다.
     */
    @Bean
    public Queue cancelDlqQueue() {
        return new Queue(CANCEL_DLQ, true);
    }

    // ===================== Binding 정의 =====================

    @Bean
    public Binding pendingBinding() {
        return BindingBuilder.bind(pendingQueue())
                .to(pendingExchange())
                .with(PENDING_ROUTING_KEY);
    }

    @Bean
    public Binding cancelBinding() {
        return BindingBuilder.bind(cancelQueue())
                .to(dlxExchange())
                .with(CANCEL_ROUTING_KEY);
    }

    @Bean
    public Binding cancelDlqBinding() {
        return BindingBuilder.bind(cancelDlqQueue())
                .to(cancelDlxExchange())
                .with(CANCEL_DLQ_ROUTING_KEY);
    }

    // ===================== 메시지 직렬화 설정 =====================

    /**
     * Java 객체 ↔ JSON 자동 변환 컨버터
     * 이 Bean이 없으면 메시지가 byte[] 형태로 직렬화되어 읽기 어렵습니다.
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /** RabbitTemplate에 JSON 컨버터 적용 */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    /**
     * MANUAL Ack 모드 컨테이너 팩토리
     * ReservationTimeoutListener에서 메시지 처리 성공/실패에 따라 직접 ACK/NACK을 호출합니다.
     * AUTO 모드에서는 예외 발생 시 메시지가 유실될 수 있지만, MANUAL 모드에서는
     * 비즈니스 로직이 완전히 성공한 경우에만 ACK를 전송하여 메시지 유실을 방지합니다.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory manualAckListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setMessageConverter(messageConverter());
        return factory;
    }
}
