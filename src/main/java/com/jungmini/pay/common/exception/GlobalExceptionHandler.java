package com.jungmini.pay.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PayException.class)
    public ResponseEntity<ErrorResponse> handlePayException(PayException e) {
        return switch (e.getErrorCode()) {
            case UN_AUTHORIZED -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(ErrorResponse.builder()
                            .errorCode(e.getErrorCode().toString())
                            .message(e.getErrorMessage())
                            .build());

            case REQUESTER_IS_NOT_OWNER -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ErrorResponse.builder()
                            .errorCode(e.getErrorCode().toString())
                            .message(e.getErrorMessage())
                            .build());

            default -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(ErrorResponse.builder()
                            .errorCode(e.getErrorCode().toString())
                            .message(e.getErrorMessage())
                            .build());
        };
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<FieldValidationErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new FieldValidationErrorResponse(e));
    }
}
