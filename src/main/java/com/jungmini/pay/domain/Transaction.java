package com.jungmini.pay.domain;

import com.jungmini.pay.common.exception.ErrorCode;
import com.jungmini.pay.common.exception.PayException;
import com.jungmini.pay.domain.type.TransactionResultType;
import com.jungmini.pay.domain.type.TransactionType;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false) // 부모 필드 값은 확인 안하도록 설정
@Entity
public class Transaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(value = EnumType.STRING)
    @EqualsAndHashCode.Exclude private TransactionType transactionType;

    @Enumerated
    @EqualsAndHashCode.Exclude private TransactionResultType transactionResultType;

    @EqualsAndHashCode.Exclude private long balanceSnapshot;

    @EqualsAndHashCode.Exclude private int amount;

    @ManyToOne
    @JoinColumn(name = "recipient_account_number")
    @EqualsAndHashCode.Exclude private Account recipientAccount;

    @ManyToOne
    @JoinColumn(name = "remitter_account_number")
    @EqualsAndHashCode.Exclude private Account remitterAccount;

    public void failTransaction(Account recipientAccount, Account remitterAccount, TransactionType transactionType) {
        enrollRecipientAccount(recipientAccount);
        enrollRemitterAccount(remitterAccount);
        enrollTransactionType(transactionType);
        enrollTransactionResultType(TransactionResultType.FAIL);
        saveSnapshot();
    }

    private void enrollTransactionResultType(TransactionResultType transactionResultType) {
        this.transactionResultType = transactionResultType;
    }

    private void enrollTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public void successTransaction(Account recipientAccount, Account remitterAccount, TransactionType transactionType) {
        enrollRecipientAccount(recipientAccount);
        enrollRemitterAccount(remitterAccount);
        enrollTransactionType(transactionType);
        enrollTransactionResultType(TransactionResultType.SUCCESS);
        validate();
        saveSnapshot();
        exChangeAmount();
    }

    private void enrollRecipientAccount(Account account) {
        this.recipientAccount = account;
    }

    private void enrollRemitterAccount(Account account) {
        this.remitterAccount = account;
    }

    private void validate() {
        if (this.remitterAccount == null) {
            throw new PayException(ErrorCode.ILLEGAL_TRANSACTION_STATE);
        }

        if (this.recipientAccount == null) {
            throw new PayException(ErrorCode.ILLEGAL_TRANSACTION_STATE);
        }

        if (this.transactionType == TransactionType.CANCEL && this.amount > this.remitterAccount.getBalance()) {
            throw new PayException(ErrorCode.LACK_OF_BALANCE);
        }
    }

    private void saveSnapshot() {
        this.balanceSnapshot = this.remitterAccount.getBalance();
    }

    private void exChangeAmount() {
        this.remitterAccount.minusAmount(this.amount);
        this.recipientAccount.plusAmount(this.amount);
    }
}
