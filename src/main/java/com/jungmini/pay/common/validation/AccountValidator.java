package com.jungmini.pay.common.validation;

import com.jungmini.pay.common.annotation.AccountNumberCheck;
import com.jungmini.pay.domain.Account;
import com.jungmini.pay.exception.PayException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 계좌 번호를 검증하는 Validator
 * 계좌 번호는 12 자리 숫자여야 한다.
 */
public class AccountValidator implements ConstraintValidator<AccountNumberCheck, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            Account.validateAccountNumber(value);
            return true;
        } catch (PayException e) {
            return false;
        }
    }
}
