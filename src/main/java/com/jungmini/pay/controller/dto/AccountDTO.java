package com.jungmini.pay.controller.dto;

import com.jungmini.pay.domain.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
