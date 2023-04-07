package com.jungmini.pay.common.exception;

import lombok.Getter;

@Getter
public class PayException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String errorMessage;

    public PayException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getDescription();
    }
}
