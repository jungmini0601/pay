package com.jungmini.pay.controller.dto;

import com.jungmini.pay.common.annotation.AccountNumberCheck;
import com.jungmini.pay.domain.Account;
import com.jungmini.pay.domain.AccountNumber;
import com.jungmini.pay.domain.Transaction;

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

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RemitRequest {

        @Min(value = 1, message = "최소 송금 금액은 1원 입니다.")
        @Max(value = 2000000, message = "1회 송금 금액은 200만원 입니다.")
        private int amount;

        @AccountNumberCheck
        private String recipientsAccountNumber;

        @AccountNumberCheck
        private String remitterAccountNumber;

        public Transaction toTransaction() {

            Account recipient = Account.builder()
                    .accountNumber(recipientsAccountNumber)
                    .build();

            Account remitter = Account.builder()
                    .accountNumber(remitterAccountNumber)
                    .build();

            return Transaction.builder()
                    .amount(amount)
                    .recipientAccount(recipient)
                    .remitterAccount(remitter)
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RemitResponse {

        private int amount;
        private String recipientsAccountNumber;
        private String remitterAccountNumber;
        private LocalDateTime createdAt;

        public static RemitResponse from(Transaction transaction) {
            return RemitResponse.builder()
                    .amount(transaction.getAmount())
                    .recipientsAccountNumber(transaction.getRecipientAccount().getAccountNumber())
                    .remitterAccountNumber(transaction.getRemitterAccount().getAccountNumber())
                    .createdAt(transaction.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GetAccountResponse {

        private long balance;
        private String accountNumber;
        private String ownerEmail;
        private LocalDateTime createdAt;

        public static GetAccountResponse from(Account account) {
            return GetAccountResponse.builder()
                    .balance(account.getBalance())
                    .ownerEmail(account.getOwner().getEmail())
                    .createdAt(account.getCreatedAt())
                    .accountNumber(account.getAccountNumber())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GetTransactionResponse {

        private long id;
        private String transactionType;
        private String transactionResultType;
        private int amount;
        private String recipientAccountNumber;
        private String remitterAccountNumber;

        public static GetTransactionResponse from(Transaction transaction) {
            return GetTransactionResponse.builder()
                    .id(transaction.getId())
                    .transactionType(transaction.getTransactionType().toString())
                    .transactionResultType(transaction.getTransactionResultType().toString())
                    .amount(transaction.getAmount())
                    .recipientAccountNumber(transaction.getRecipientAccount().getAccountNumber())
                    .remitterAccountNumber(transaction.getRemitterAccount().getAccountNumber())
                    .build();
        }
    }
}
