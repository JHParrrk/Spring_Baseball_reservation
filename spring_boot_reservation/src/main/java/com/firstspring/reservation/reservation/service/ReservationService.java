package com.firstspring.reservation.reservation.service;

import com.firstspring.reservation.reservation.dto.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * [스프링 입문] 예약 서비스 인터페이스입니다.
 *
 * 실제 비즈니스 로직은 ReservationServiceImpl에 구현됩니다.
 *
 * 인터페이스를 사용하는 이유:
 * 1) 컨트롤러는 구현체에 직접 의존하지 않아 코드 변경이 컴하지 않습니다 (느슨한 결합)
 * 2) 테스트 시 Mock으로 대체하기 쉽습니다
 * 3) 향후 다른 구현체로 교체할 수 있습니다
 */
public interface ReservationService {

    // 페이지네이션 적용: 대용량 데이터 OOM 방지
    Page<ReservationResponse> getReservationsByUser(Long userId, Pageable pageable);

    // expectedUserId : JWT 토큰에서 추출한 사용자 ID. 소유권 검증에 사용
    ReservationResponse getReservation(Long id, Long expectedUserId);

    ReservationResponse cancelReservation(Long id, Long expectedUserId);

    void deleteReservation(Long id, Long expectedUserId);

    /** payment 서비스로부터 결제 성공 수신 시 예약 상태를 CONFIRMED로 업데이트 */
    void confirmReservation(Long reservationId);

    /** payment 서비스로부터 결제 실패 수신 시 예약 취소 처리 + 좌석 복원 */
    void cancelReservationByPaymentFailure(Long reservationId);

    /**
     * '내 예약'에서 사용자가 선택한 PENDING 예약들에 대해 Kafka 결제 이벤트를 발행합니다.
     * @param reservationIds 결제할 예약 ID 목록 (본인 소유 + PENDING 상태여야 함)
     * @param userId         JWT에서 추출한 현재 사용자 ID
     * @param cvc            결제 CVC
     */
    List<ReservationResponse> payReservations(List<Long> reservationIds, Long userId, String cvc);

    /**
     * 관리자 전용 예약 강제 취소.
     * 비관적 락을 사용하여 사용자 cancelReservation / RabbitMQ 타임아웃과의 Race Condition을 방지합니다.
     */
    ReservationResponse forceCancel(Long reservationId);
}
