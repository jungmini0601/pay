package com.jungmini.pay.domain;

import com.jungmini.pay.exception.ErrorCode;
import com.jungmini.pay.exception.PayException;
import com.jungmini.pay.type.AccountStatus;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Account extends BaseTimeEntity implements AccountNumber {

    @Id
    private String accountNumber;

    @Enumerated(value = EnumType.STRING)
    private AccountStatus accountStatus;

    @ManyToOne
    @JoinColumn(name = "email")
    private Member owner;

    private long balance;

    public static final String DEFAULT_ACCOUNT_NUMBER = "100000000000";
    public static final int MAX_ACCOUNT_SIZE = 10;

    public static Account from(Member owner, AccountNumber accountNumber) {
        Account newAccount = Account.builder()
                .balance(0)
                .accountNumber(createNextAccount(accountNumber))
                .owner(owner)
                .accountStatus(AccountStatus.IN_USE)
                .build();

        newAccount.validateAccountNumber();

        return newAccount;
    }

    public void chargePoint(int amount, Member requester) {
        if (!this.owner.equals(requester)) {
            throw new PayException(ErrorCode.REQUESTER_IS_NOT_OWNER);
        }

        this.balance += amount;
    }

    /**
     * 계좌 번호는 12자리 숫자로 이루어진 문자열이어야 한다.
     */
    public static void validateAccountNumber(String accountNumber) {
        if (accountNumber == null)
            throw new PayException(ErrorCode.ILLEGAL_ACCOUNT_NUMBER);

        if (accountNumber.length() != 12)
            throw new PayException(ErrorCode.ILLEGAL_ACCOUNT_NUMBER);

        accountNumber.chars().forEach(c -> {
            if (c < '0' || c > '9')
                throw new PayException(ErrorCode.ILLEGAL_ACCOUNT_NUMBER);
        });
    }

    private void validateAccountNumber() {
        validateAccountNumber(this.accountNumber);
    }

    private static String createNextAccount(AccountNumber accountNumber) {
        if (accountNumber == null)
            return DEFAULT_ACCOUNT_NUMBER;

        return Long.parseLong(accountNumber.getAccountNumber()) + 1 + "";
    }
}
