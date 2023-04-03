package com.jungmini.pay.controller.dto;

import com.jungmini.pay.common.annotation.AccountNumberCheck;
import com.jungmini.pay.domain.Account;
import com.jungmini.pay.domain.AccountNumber;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class AccountDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateAccountResponse {
        private String accountNumber;
        private String accountStatus;

        public static CreateAccountResponse from(Account account) {
            return CreateAccountResponse
                    .builder()
                    .accountStatus(account.getAccountStatus().toString())
                    .accountNumber(account.getAccountNumber())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChargePointRequest implements AccountNumber {

        @Min(value = 10000, message = "최소 충전 금액은 1만원 입니다.")
        @Max(value = 2000000, message = "1회 최대 충전 금액은 200만원 입니다.")
        private int amount;

        @AccountNumberCheck
        private String accountNumber;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChargePointResponse {
        private long balance;
        private String accountNumber;
        private LocalDateTime createdAt; // 충전시간

        public static ChargePointResponse from(Account account) {
            return ChargePointResponse.builder()
                    .balance(account.getBalance())
                    .accountNumber(account.getAccountNumber())
                    .createdAt(LocalDateTime.now())
                    .build();
        }

    }
}
