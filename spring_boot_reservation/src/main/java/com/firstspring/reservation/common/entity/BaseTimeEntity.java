package com.firstspring.reservation.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * [공통] 엔티티 생성/수정 시간을 자동으로 관리하는 베이스 클래스입니다.
 *
 * @MappedSuperclass : 상속받는 자식 엔티티들에게 매핑 정보를 제공합니다.
 *                   @EntityListeners(AuditingEntityListener.class) : JPA
 *                   Auditing 기능을 활성화합니다.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
