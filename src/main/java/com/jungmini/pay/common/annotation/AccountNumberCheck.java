package com.jungmini.pay.common.annotation;

import com.jungmini.pay.common.validation.AccountValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * custom bean validation 적용하기 위한 어노테이션
 * 이 어노테이션이 붙은 필드는 AccountValidator 로 검증한다.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AccountValidator.class)
public @interface AccountNumberCheck {
    String message() default "계좌 번호는 12자리 숫자여야 합니다.";

    Class<?>[] groups() default {}; // TODO 뭔지 알아보기

    Class<? extends Payload>[] payload() default {}; // TODO 뭔지 알아보기
}
