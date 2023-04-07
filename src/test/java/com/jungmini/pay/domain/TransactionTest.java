package com.jungmini.pay.domain;

import com.jungmini.pay.common.exception.ErrorCode;
import com.jungmini.pay.common.exception.PayException;
import com.jungmini.pay.domain.type.TransactionResultType;
import com.jungmini.pay.domain.type.TransactionType;
import com.jungmini.pay.fixture.AccountFactory;
import com.jungmini.pay.fixture.MemberFactory;
import com.jungmini.pay.fixture.TransactionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    @DisplayName("송금 성공")
    void remit_success() {
        int remitterBalance = 10000;
        int recipientBalance = 100;
        int amount = 500;
        Member remitter = MemberFactory.memberFrom("remitter@test.com");
        Member recipient = MemberFactory.memberFrom("recipient@test.com");
        Account remitterAccount = AccountFactory.accountFromOwnerAndBalance(remitter, remitterBalance);
        Account recipientAccount = AccountFactory.accountFromOwnerAndBalance(recipient, recipientBalance);
        Transaction transactionRequest = TransactionFactory
                .transactionRequest(remitterAccount, recipientAccount, amount);

        transactionRequest.successTransaction(recipientAccount, remitterAccount, TransactionType.REMIT);

        assertThat(transactionRequest.getTransactionType()).isEqualTo(TransactionType.REMIT);
        assertThat(transactionRequest.getTransactionResultType()).isEqualTo(TransactionResultType.SUCCESS);
        assertThat(transactionRequest.getRemitterAccount()).isEqualTo(remitterAccount);
        assertThat(transactionRequest.getBalanceSnapshot()).isEqualTo(remitterBalance);
        assertThat(transactionRequest.getAmount()).isEqualTo(amount);
        assertThat(transactionRequest.getRecipientAccount().getBalance()).isEqualTo(recipientBalance + amount);
        assertThat(transactionRequest.getRemitterAccount().getBalance()).isEqualTo(remitterBalance - amount);
    }

    @Test
    @DisplayName("송금 실패 - 수신자 계좌 정보 없음")
    void remit_fail_recipient_account_null() {
        int amount = 500;
        Member remitter = MemberFactory.memberFrom("remitter@test.com");
        Account remitterAccount = AccountFactory.accountFromOwnerAndBalance(remitter, 10000);

        Transaction transactionRequest = TransactionFactory
                .transactionRequest(null, remitterAccount, amount);

        PayException payException = assertThrows(PayException.class, () -> {
            transactionRequest.successTransaction(null, remitterAccount, TransactionType.REMIT);
        });

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_TRANSACTION_STATE);
    }

    @Test
    @DisplayName("송금 실패 - 송금자 계좌 정보 없음")
    void remit_fail_remitter_account_null() {
        int amount = 500;
        Member remitter = MemberFactory.memberFrom("remitter@test.com");
        Account remitterAccount = AccountFactory.accountFromOwnerAndBalance(remitter, 10000);

        Transaction transactionRequest = TransactionFactory
                .transactionRequest(remitterAccount, null, amount);

        PayException payException = assertThrows(PayException.class, () -> {
            transactionRequest.successTransaction(remitterAccount, null, TransactionType.REMIT);
        });

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_TRANSACTION_STATE);
    }

    @Test
    @DisplayName("송금 실패 - 잔액 부족")
    void create_transaction_fail_lock_of_balance() {
        int amount = 50000;
        Member remitter = MemberFactory.memberFrom("remitter@test.com");
        Member recipient = MemberFactory.memberFrom("recipient@test.com");
        Account remitterAccount = AccountFactory.accountFromOwnerAndBalance(remitter, 10000);
        Account recipientAccount = AccountFactory.accountFromOwnerAndBalance(recipient, 100);
        Transaction transactionRequest = TransactionFactory
                .transactionRequest(remitterAccount, recipientAccount, amount);

        PayException payException = assertThrows(PayException.class, () -> {
            transactionRequest.successTransaction(recipientAccount, remitterAccount, TransactionType.REMIT);
        });

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.LACK_OF_BALANCE);
    }
}