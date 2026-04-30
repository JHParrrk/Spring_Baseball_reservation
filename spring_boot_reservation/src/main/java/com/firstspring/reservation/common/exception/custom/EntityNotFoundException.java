package com.firstspring.reservation.common.exception.custom;

import org.springframework.http.HttpStatus;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외입니다.
 */
public class EntityNotFoundException extends BusinessException {
    public EntityNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
