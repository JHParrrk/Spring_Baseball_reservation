package com.firstspring.reservation.common.exception.custom;

import org.springframework.http.HttpStatus;

/**
 * 경기/좌석 등을 찾을 수 없을 때 발생하는 예외입니다.
 */
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
