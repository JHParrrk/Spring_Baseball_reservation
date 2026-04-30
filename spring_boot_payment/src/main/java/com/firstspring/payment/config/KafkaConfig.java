package com.firstspring.payment.config;

import com.firstspring.payment.dto.PaymentResultEvent;
import com.firstspring.payment.dto.ReservationSuccessEvent;
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
 * Payment 서비스 Kafka 설정
 *
 * Consumer: reservation.success 토픽 구독 (예약 성공 이벤트 수신)
 * Producer: payment.result 토픽 발행 (결제 결과 이벤트 발행)
 */
@Configuration
public class KafkaConfig {

    /** 소비할 토픽: reservation 서비스가 발행한 예약 성공 이벤트 */
    public static final String RESERVATION_SUCCESS_TOPIC = "reservation.success";

    /** 발행할 토픽: 결제 결과를 reservation 서비스로 전달 */
    public static final String PAYMENT_RESULT_TOPIC = "payment.result";

    /** Consumer Group ID */
    public static final String PAYMENT_GROUP_ID = "payment-group";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ===================== Topic =====================

    @Bean
    public org.apache.kafka.clients.admin.NewTopic paymentResultTopic() {
        return TopicBuilder.name(PAYMENT_RESULT_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    // ===================== Consumer (ReservationSuccessEvent) =====================

    @Bean
    public ConsumerFactory<String, ReservationSuccessEvent> reservationEventConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, PAYMENT_GROUP_ID);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        JsonDeserializer<ReservationSuccessEvent> deserializer =
                new JsonDeserializer<>(ReservationSuccessEvent.class, false);
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ReservationSuccessEvent>
    reservationEventListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ReservationSuccessEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(reservationEventConsumerFactory());
        // 3회 재시도, 1초 간격
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 3)));
        return factory;
    }

    // ===================== Producer (PaymentResultEvent) =====================

    @Bean
    public ProducerFactory<String, PaymentResultEvent> paymentResultProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, PaymentResultEvent> paymentResultKafkaTemplate() {
        return new KafkaTemplate<>(paymentResultProducerFactory());
    }
}
