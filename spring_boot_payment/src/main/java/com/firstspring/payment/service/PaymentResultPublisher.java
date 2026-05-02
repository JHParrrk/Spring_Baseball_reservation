package com.firstspring.payment.service;

import com.firstspring.payment.config.KafkaConfig;
import com.firstspring.payment.dto.PaymentResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * [Kafka] 결제 결과 이벤트 발행 서비스 (Producer)
 *
 * 결제 처리 완료 후 payment.result 토픽으로 결과를 발행합니다.
 * reservation 서비스가 이 이벤트를 수신하여 예약 상태를 업데이트합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentResultPublisher {

    private final KafkaTemplate<String, PaymentResultEvent> paymentResultKafkaTemplate;

    public void publishResult(PaymentResultEvent event) {
        try {
            paymentResultKafkaTemplate.send(
                    KafkaConfig.PAYMENT_RESULT_TOPIC,
                    String.valueOf(event.reservationId()),
                    event).get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("[Kafka] 결제 결과 이벤트 발행 실패. reservationId=" + event.reservationId(), e);
        }

        log.info("[Kafka] 결제 결과 이벤트 발행 완료. reservationId={}, status={}, paymentId={}",
                event.reservationId(), event.status(), event.paymentId());
    }
}
