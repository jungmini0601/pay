package com.jungmini.pay.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private String errorCode;
    private String message;
}
