package com.firstspring.reservation.common.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * [공통] 에러 응답 포맷을 정의하는 DTO입니다.
 */
@Getter
@Builder
public class ErrorResponse {
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String error;
    private final String message;
    private final String path;
}
