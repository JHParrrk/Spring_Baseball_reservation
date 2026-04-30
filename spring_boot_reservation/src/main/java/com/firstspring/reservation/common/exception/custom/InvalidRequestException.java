package com.firstspring.reservation.common.exception.custom;

import org.springframework.http.HttpStatus;

/**
 * 경기 시간 만료, 이미 점유된 좌석 등 잘못된 요청 시 발생하는 예외입니다.
 */
public class InvalidRequestException extends BusinessException {
    public InvalidRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
