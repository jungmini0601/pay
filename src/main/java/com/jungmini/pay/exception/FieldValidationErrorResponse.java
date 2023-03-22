package com.jungmini.pay.exception;

import lombok.Getter;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
public class FieldValidationErrorResponse {
    private String errorCode;
    private Map<String, String> errorFields;

    public FieldValidationErrorResponse(MethodArgumentNotValidException e) {
        this.errorFields = makeErrorMessage(e.getBindingResult().getAllErrors());
        this.errorCode = ErrorCode.BAD_REQUEST.toString();
    }

    private static Map<String, String> makeErrorMessage(List<ObjectError> errors) {
        Map<String, String> errorMap = new HashMap<>();
        errors.forEach(error -> {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        });
        return errorMap;
    }
}
