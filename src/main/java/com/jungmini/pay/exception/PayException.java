package com.jungmini.pay.exception;

import lombok.Getter;

@Getter
public class PayException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;

    public PayException(ErrorCode errorCode) {
        this.errorCode = errorCode.toString();
        this.errorMessage = errorCode.getDescription();
    }
}
