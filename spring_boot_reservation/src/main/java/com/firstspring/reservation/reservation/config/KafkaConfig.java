package com.firstspring.reservation.reservation.config;

import com.firstspring.reservation.reservation.dto.PaymentResultEvent;
import com.firstspring.reservation.reservation.dto.ReservationSuccessEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * [Kafka] 예약 성공 이벤트 토픽/직렬화 설정
 *
 * InfraTestService의 기본 KafkaTemplate<String, String>과 충돌 없이 동작하도록
 * 별도의 ProducerFactory / ConsumerFactory를 정의합니다.
 *
 * reservationKafkaTemplate : 이벤트 발행에 사용 (JSON 직렬화)
 * reservationKafkaListenerContainerFactory : @KafkaListener에서 containerFactory
 * 속성으로 지정
 */
@Configuration
public class KafkaConfig {

    /** 예약 성공 이벤트를 발행할 토픽 이름 */
    public static final String RESERVATION_SUCCESS_TOPIC = "reservation.success";

    /** payment 서비스로부터 결제 결과를 수신할 토픽 이름 */
    public static final String PAYMENT_RESULT_TOPIC = "payment.result";

    /** 알림 Consumer의 Group ID */
    public static final String NOTIFICATION_GROUP_ID = "notification-group";

    /** 결제 결과 Consumer의 Group ID */
    public static final String PAYMENT_RESULT_GROUP_ID = "reservation-payment-group";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ===================== Topic =====================

    /**
     * 앱 시작 시 토픽이 없으면 자동 생성합니다.
     * 이미 있으면 무시됩니다.
     */
    @Bean
    public org.apache.kafka.clients.admin.NewTopic reservationSuccessTopic() {
        return TopicBuilder.name(RESERVATION_SUCCESS_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    // ===================== Producer =====================

    /**
     * InfraTestService가 사용하는 기본 String 타입 KafkaTemplate 입니다.
     * 커스텀 KafkaTemplate 빈을 정의하면 Spring Boot 자동 구성이 비활성화되므로
     * 이 빈을 직접 등록하여 대체합니다.
     */
    @Bean
    public ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }

    @Bean
    public ProducerFactory<String, ReservationSuccessEvent> reservationProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // false: 타입 헤더를 포함하지 않습니다.
        // payment 서비스의 Consumer가 자체 DTO 클래스로 역직렬화하므로
        // 헤더에 reservation 서비스 패키지 경로가 담기면 오히려 혼란을 줍니다.
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * 이름을 명시하여 Spring Boot 자동 구성 KafkaTemplate과 충돌을 방지합니다.
     * ReservationEventPublisher에서 @Qualifier("reservationKafkaTemplate")로 주입합니다.
     */
    @Bean("reservationKafkaTemplate")
    public KafkaTemplate<String, ReservationSuccessEvent> reservationKafkaTemplate() {
        return new KafkaTemplate<>(reservationProducerFactory());
    }

    // ===================== Consumer =====================

    @Bean
    public ConsumerFactory<String, ReservationSuccessEvent> reservationConsumerFactory() {
        JsonDeserializer<ReservationSuccessEvent> deserializer = new JsonDeserializer<>(ReservationSuccessEvent.class,
                false);
        // 특정 패키지만 허용 — "*" 와일드카드는 역직렬화 가젯 체인 공격 위험이 있어 사용 금지
        deserializer.addTrustedPackages("com.firstspring.reservation.reservation.dto");

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, NOTIFICATION_GROUP_ID);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                deserializer);
    }

    /**
     * 이름을 명시하여 @KafkaListener(containerFactory =
     * "reservationKafkaListenerContainerFactory")
     * 에서 참조합니다.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ReservationSuccessEvent> reservationKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ReservationSuccessEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(reservationConsumerFactory());
        // 1초 간격으로 최대 3회 재시도 후 포기 (Dead Letter Topic으로 보내거나 스킵)
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 3)));
        return factory;
    }

    // ===================== Consumer (PaymentResultEvent) =====================

    @Bean
    public ConsumerFactory<String, PaymentResultEvent> paymentResultConsumerFactory() {
        JsonDeserializer<PaymentResultEvent> deserializer = new JsonDeserializer<>(PaymentResultEvent.class, false);
        deserializer.addTrustedPackages("com.firstspring.payment.dto", "com.firstspring.reservation.reservation.dto");

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, PAYMENT_RESULT_GROUP_ID);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentResultEvent> paymentResultListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PaymentResultEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentResultConsumerFactory());
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 3)));
        return factory;
    }
}
