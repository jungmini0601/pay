package com.jungmini.pay.service;

import com.jungmini.pay.domain.Account;
import com.jungmini.pay.domain.AccountNumber;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.domain.Transaction;
import com.jungmini.pay.common.exception.ErrorCode;
import com.jungmini.pay.common.exception.PayException;

import com.jungmini.pay.domain.type.TransactionType;
import com.jungmini.pay.repository.AccountRepository;
import com.jungmini.pay.repository.FriendRepository;
import com.jungmini.pay.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jungmini.pay.domain.Account.MAX_ACCOUNT_SIZE;

@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FriendRepository friendRepository;

    @Transactional
    public Account createAccount(Member owner) {
        validateAccountSize(owner);

        AccountNumber recentAccountNumber = accountRepository.findFirstByOrderByCreatedAtDesc()
                .orElse(null);

        Account newAccount = Account.from(owner, recentAccountNumber);

        return accountRepository.save(newAccount);
    }

    @Transactional
    public Account chargePoint(int amount, AccountNumber accountNumber, Member requester) {
        Account account = accountRepository.findById(accountNumber.getAccountNumber())
                .orElseThrow(() -> new PayException(ErrorCode.ACCOUNT_NOT_FOUND));

        account.chargePoint(amount, requester);
        return account;
    }

    @Transactional
    public Transaction remit(Transaction transactionRequest, Member remitter) {
        try {
            Account recipientAccount = accountRepository
                    .findById(transactionRequest.getRecipientAccount().getAccountNumber())
                    .orElseThrow(() -> new PayException(ErrorCode.ACCOUNT_NOT_FOUND));

            Account remitterAccount = accountRepository
                    .findById(transactionRequest.getRemitterAccount().getAccountNumber())
                    .orElseThrow(() -> new PayException(ErrorCode.ACCOUNT_NOT_FOUND));

            validateOwner(remitter, remitterAccount);
            validateFriendRelation(remitter, recipientAccount);
            transactionRequest.successTransaction(recipientAccount, remitterAccount, TransactionType.REMIT);
            return transactionRepository.save(transactionRequest);
        } catch (PayException e) {
            // 계좌 번호가 유효했을 경우에는 거래 정보 저장한다.
            if (!e.getErrorCode().equals("ACCOUNT_NOT_FOUND")) { // TODO PayException errorCode Enum 변환 필요
                Account recipientAccount = accountRepository
                        .findById(transactionRequest.getRecipientAccount().getAccountNumber())
                        .orElseThrow(() -> new PayException(ErrorCode.ACCOUNT_NOT_FOUND));

                Account remitterAccount = accountRepository
                        .findById(transactionRequest.getRemitterAccount().getAccountNumber())
                        .orElseThrow(() -> new PayException(ErrorCode.ACCOUNT_NOT_FOUND));

                transactionRequest.failTransaction(recipientAccount, remitterAccount, TransactionType.REMIT);
                transactionRepository.save(transactionRequest);
            }

            throw e;
        }
    }

    private static void validateOwner(Member remitter, Account remitterAccount) {
        if (!remitter.equals(remitterAccount.getOwner())) {
            throw new PayException(ErrorCode.REQUESTER_IS_NOT_OWNER);
        }
    }

    private void validateFriendRelation(Member remitter, Account recipientAccount) {
        boolean relation1 = friendRepository // remitter가 친구 요청을 보낸 경우
                .existsFriendByRecipientAndRequester(remitter, recipientAccount.getOwner());

        boolean relation2 = friendRepository // recipient가 친구 요청을 보낸 경우
                .existsFriendByRecipientAndRequester(recipientAccount.getOwner(), remitter);

        if (!relation1 && !relation2) {
            throw new PayException(ErrorCode.NOT_FRIENDS);
        }
    }

    private void validateAccountSize(Member owner) {
        int accountCount = accountRepository.countByOwner(owner);

        if (accountCount >= MAX_ACCOUNT_SIZE) {
            throw new PayException(ErrorCode.ACCOUNT_SIZE_EXCEED);
        }
    }
}
