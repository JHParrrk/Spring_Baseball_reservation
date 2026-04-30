package com.firstspring.payment.service;

import com.firstspring.payment.dto.PaymentResultEvent;
import com.firstspring.payment.dto.ReservationSuccessEvent;
import com.firstspring.payment.entity.Payment;
import com.firstspring.payment.event.PaymentProcessedEvent;
import com.firstspring.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Mock 결제 처리 서비스
 *
 * 실제 PG 연동 없이 결제를 시뮬레이션합니다.
 * - CVC 마지막 자리 0~3: 다양한 실패 시나리오
 * - CVC 마지막 자리 4~9: 결제 성공
 *
 * 멱등성 처리:
 * 동일 reservationId로 중복 이벤트가 들어오면 기존 결과를 그대로 반환합니다.
 *
 * 유령 메시지 방지:
 * Kafka 발행은 @Transactional 안에서 직접 하지 않고,
 * Spring Event → @TransactionalEventListener(AFTER_COMMIT) 를 통해
 * DB 커밋 성공 후에만 발행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void processPayment(ReservationSuccessEvent event) {
        Long reservationId = event.reservationId();

        // 멱등성: 이미 처리된 결제면 스킵
        if (paymentRepository.findByReservationId(reservationId).isPresent()) {
            log.warn("[Payment] 이미 처리된 예약입니다. reservationId={} — 중복 이벤트 무시", reservationId);
            return;
        }

        // save()의 반환값을 반드시 재할당해야 합니다.
        // JPA IDENTITY 전략에서는 save() 이후 반환된 객체에만 생성된 ID가 채워집니다.
        Payment payment = paymentRepository.save(new Payment(reservationId, event.userId()));

        // Mock 시나리오: CVC의 마지막 자리에 따른 다양한 결제 결과
        String cvc = event.cvc() != null ? event.cvc() : "001";
        char lastChar = cvc.charAt(cvc.length() - 1);

        boolean isSuccess = false;
        String failureReason = null;

        switch (lastChar) {
            case '0':
                failureReason = "결제 실패: 잔액 부족 (CVC 끝자리 0)";
                break;
            case '1':
                failureReason = "결제 실패: 카드 한도 초과 (CVC 끝자리 1)";
                break;
            case '2':
                failureReason = "결제 실패: 분실/도난 카드 (CVC 끝자리 2)";
                break;
            case '3':
                failureReason = "결제 실패: 할부 불가능 카드 (CVC 끝자리 3)";
                break;
            default:
                isSuccess = true;
                break;
        }

        PaymentResultEvent resultEvent;
        if (isSuccess) {
            payment.markSuccess();
            resultEvent = new PaymentResultEvent(
                    payment.getId(),
                    reservationId,
                    event.userId(),
                    "SUCCESS",
                    null,
                    LocalDateTime.now());
            log.info("[Payment] 결제 성공. reservationId={}, paymentId={}", reservationId, payment.getId());
        } else {
            payment.markFailed(failureReason);
            resultEvent = new PaymentResultEvent(
                    payment.getId(),
                    reservationId,
                    event.userId(),
                    "FAILED",
                    failureReason,
                    LocalDateTime.now());
            log.warn("[Payment] 결제 실패. reservationId={}, reason={}", reservationId, failureReason);
        }

        // DB 커밋 후 Kafka 발행 (유령 메시지 방지)
        eventPublisher.publishEvent(new PaymentProcessedEvent(resultEvent));
    }
}
