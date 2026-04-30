package com.firstspring.reservation.common.exception.custom;

import org.springframework.http.HttpStatus;

/**
 * 권한이 없는 작업 시 발생하는 예외입니다.
 */
public class UnauthorizedAccessException extends BusinessException {
    public UnauthorizedAccessException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
