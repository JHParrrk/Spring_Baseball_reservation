package com.firstspring.reservation.reservation.service;

import com.firstspring.reservation.reservation.config.KafkaConfig;
import com.firstspring.reservation.reservation.dto.ReservationSuccessEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * [Kafka] 예약 성공 이벤트 수신 및 알림 처리 Consumer
 *
 * 'reservation.success' 토픽을 구독합니다.
 * ReservationEventPublisher가 발행한 이벤트를 수신하여 사용자 알림을 처리합니다.
 *
 * 실제 서비스에서는 이 Consumer가 별도 마이크로서비스(알림 서비스)로 분리됩니다.
 * 이 구현은 동일 애플리케이션 내 시뮬레이션이며,
 * sendNotification() 메서드에 이메일/SMS/푸시 알림 연동 코드를 추가하면 됩니다.
 *
 * containerFactory:
 * 'reservationKafkaListenerContainerFactory' — KafkaConfig에서 정의한
 * JSON 역직렬화 컨테이너 팩토리를 사용합니다.
 * (기본 String 팩토리가 아닌 JSON 전용 팩토리를 명시적으로 지정)
 */
@Slf4j
@Service
public class ReservationNotificationConsumer {

    @KafkaListener(topics = KafkaConfig.RESERVATION_SUCCESS_TOPIC, groupId = KafkaConfig.NOTIFICATION_GROUP_ID, containerFactory = "reservationKafkaListenerContainerFactory")
    public void handleReservationSuccess(ReservationSuccessEvent event) {
        log.info("[Kafka] 예약 성공 이벤트 수신. reservationId={}, userId={}, 좌석={} ({}), 경기='{}'",
                event.reservationId(),
                event.userId(),
                event.seatNumber(),
                event.tier(),
                event.matchTitle());

        sendNotification(event);
    }

    /**
     * 사용자 알림 발송 처리
     *
     * 현재는 로그로 시뮬레이션합니다.
     * 실제 연동 시 이 메서드 내부에 구현하세요:
     * - 이메일: JavaMailSender, SendGrid 등
     * - 푸시 알림: Firebase FCM
     * - SMS: AWS SNS, Twilio 등
     */
    private void sendNotification(ReservationSuccessEvent event) {
        // TODO: 실제 알림 서비스 연동
        log.info("[Notification] 알림 발송 완료 → userId={} | 경기: '{}' | 좌석: {} ({}) | 예약ID: {}",
                event.userId(),
                event.matchTitle(),
                event.seatNumber(),
                event.tier(),
                event.reservationId());
    }
}
