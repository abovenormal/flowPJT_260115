package com.example.extensionCheck.api.exception;

import com.example.extensionCheck.api.response.ApiErrorResponse;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 커스텀 확장자 예외 처리
     */
    @ExceptionHandler(ExtensionException.class)
    public ResponseEntity<ApiErrorResponse> handleExtensionException(ExtensionException e) {
        ExtensionErrorCode errorCode = e.getErrorCode();
        HttpStatus status = mapToHttpStatus(errorCode);

        log.warn("Extension exception: code={}, message={}", errorCode.getCode(), e.getMessage());

        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(errorCode.getCode(), e.getMessage()));
    }

    /**
     * 잘못된 입력값 (validation 실패)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse("BAD_REQUEST", e.getMessage()));
    }

    /**
     * 이미 존재하는 데이터
     */
    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityExists(EntityExistsException e) {
        log.warn("Entity exists: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse("CONFLICT", e.getMessage()));
    }

    /**
     * 데이터를 찾을 수 없음
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFound(EntityNotFoundException e) {
        log.warn("Entity not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("NOT_FOUND", e.getMessage()));
    }

    /**
     * 기타 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse("INTERNAL_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }

    /**
     * ExtensionErrorCode를 HTTP 상태 코드로 매핑
     */
    private HttpStatus mapToHttpStatus(ExtensionErrorCode errorCode) {
        return switch (errorCode) {
            case EMPTY_INPUT, TOO_LONG, CONTAINS_DIGIT, CONTAINS_KOREAN -> HttpStatus.BAD_REQUEST;  // Http Status : 400
            case ALREADY_EXISTS, FIXED_EXTENSION_CONFLICT -> HttpStatus.CONFLICT;                   // Http Status : 409
            case NOT_FOUND -> HttpStatus.NOT_FOUND;                                                 // Http Status : 404
            case MAX_LIMIT_EXCEEDED -> HttpStatus.UNPROCESSABLE_ENTITY;                             // Http Status : 422
        };
    }
}
