package com.firstspring.payment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Payment 엔티티 (H2 in-memory DB)
 *
 * 결제 이력을 저장합니다.
 * status: PENDING → SUCCESS | FAILED
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", nullable = false, unique = true)
    private Long reservationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public enum Status {
        PENDING, SUCCESS, FAILED
    }

    public Payment() {}

    public Payment(Long reservationId, Long userId) {
        this.reservationId = reservationId;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public Long getReservationId() { return reservationId; }
    public Long getUserId() { return userId; }
    public Status getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }

    public void markSuccess() {
        this.status = Status.SUCCESS;
        this.processedAt = LocalDateTime.now();
    }

    public void markFailed(String reason) {
        this.status = Status.FAILED;
        this.failureReason = reason;
        this.processedAt = LocalDateTime.now();
    }
}
