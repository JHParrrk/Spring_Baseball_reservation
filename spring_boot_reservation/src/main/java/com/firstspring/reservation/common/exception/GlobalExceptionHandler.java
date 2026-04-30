package com.firstspring.reservation.common.exception;

import com.firstspring.reservation.common.exception.custom.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * [공통] 애플리케이션 전역에서 발생하는 예외를 한 곳에서 처리하는 클래스입니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @Valid 또는 @Validated로 검증 실패 시 발생하는 예외를 처리합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
            HttpServletRequest request) {
        BindingResult bindingResult = e.getBindingResult();
        String message = bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("유효성 검증 실패: {}", message);
        return createErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * 비즈니스 로직 상 발생하는 커스텀 예외들을 처리합니다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("BusinessException: {}", e.getMessage());
        return createErrorResponse(e.getStatus(), e.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e,
            HttpServletRequest request) {
        log.warn("잘못된 요청 인자: {}", e.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e,
            HttpServletRequest request) {
        log.warn("잘못된 요청 상태: {}", e.getMessage());
        return createErrorResponse(HttpStatus.CONFLICT, e.getMessage(), request);
    }

    /**
     * @RequestParam 등에 잘못된 타입(예: Enum 불일치)이 전달될 때 발생하는 예외를 처리합니다.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String message = String.format("'%s' 파라미터의 값 '%s'이(가) 올바르지 않습니다.", e.getName(), e.getValue());
        log.warn("파라미터 타입 불일치: {}", message);
        return createErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * @Version 낙관적 락 충돌 시 발생하는 예외를 처리합니다.
     *
     *          Seat, MatchInfo 모두 @Version 필드를 사용합니다.
     *          동시에 두 요청이 같은 엔티티를 수정하면 나중 요청이 이 예외를 던집니다.
     *          HTTP 500(서버 오류)이 아닌 409 Conflict로 반환해야 클라이언트가 재시도를 올바르게 처리합니다.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailureException(
            ObjectOptimisticLockingFailureException e, HttpServletRequest request) {
        log.warn("낙관적 락 충돌 발생 (동시 요청): {}", e.getMessage());
        return createErrorResponse(HttpStatus.CONFLICT, "다른 요청이 동시에 처리 중입니다. 잠시 후 다시 시도해 주세요.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception e, HttpServletRequest request) {
        log.error("서버 내부 오류 발생: ", e);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", request);
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus status, String message,
            HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .status(status.value())
                .error(status.name())
                .message(message)
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(response, status);
    }
}
