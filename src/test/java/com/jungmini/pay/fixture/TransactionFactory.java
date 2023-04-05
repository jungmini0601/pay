package com.jungmini.pay.fixture;

import com.jungmini.pay.domain.Account;
import com.jungmini.pay.domain.Transaction;

public class TransactionFactory {

    public static Transaction transactionRequest(
            Account remitterAccount, Account recipientAccount, int amount) {
        return Transaction.builder()
                .remitterAccount(remitterAccount)
                .recipientAccount(recipientAccount)
                .amount(amount)
                .build();
    }

    public static Transaction from(Transaction transaction) {
        return Transaction.builder()
                .remitterAccount(transaction.getRemitterAccount())
                .recipientAccount(transaction.getRecipientAccount())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .transactionResultType(transaction.getTransactionResultType())
                .balanceSnapshot(transaction.getBalanceSnapshot())
                .build();
    }
}
