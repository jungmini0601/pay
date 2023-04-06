package com.jungmini.pay.repository;

import com.jungmini.pay.domain.Account;
import com.jungmini.pay.domain.Transaction;
import com.jungmini.pay.domain.type.TransactionResultType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByRecipientAccountOrRemitterAccountAndTransactionResultTypeOrderByCreatedAtDesc
            (Account recipientAccountNumber, Account remitterAccountNumber, TransactionResultType transactionResultType, Pageable pageable);
}
