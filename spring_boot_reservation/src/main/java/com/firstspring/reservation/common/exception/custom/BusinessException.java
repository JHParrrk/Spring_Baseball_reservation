package com.firstspring.reservation.common.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * [공통] 비즈니스 예외들의 부모 클래스입니다.
 */
@Getter
public abstract class BusinessException extends RuntimeException {
    private final HttpStatus status;

    protected BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
