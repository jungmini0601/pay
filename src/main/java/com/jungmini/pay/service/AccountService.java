package com.jungmini.pay.service;

import com.jungmini.pay.domain.Account;
import com.jungmini.pay.domain.AccountNumber;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.domain.Transaction;
import com.jungmini.pay.common.exception.ErrorCode;
import com.jungmini.pay.common.exception.PayException;

import com.jungmini.pay.domain.type.TransactionResultType;
import com.jungmini.pay.domain.type.TransactionType;
import com.jungmini.pay.repository.AccountRepository;
import com.jungmini.pay.repository.FriendRepository;
import com.jungmini.pay.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                        .findById(transactionRequest.getRecipientAccount().getAccountNumber()) // TODO 코드 중복 제거 예정
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

    @Transactional(readOnly = true)
    public Account getAccountInfo(String accountNumber, Member member) { // TODO member 파라미터 이름 수정
        Account account = accountRepository.findById(accountNumber) // TODO 리팩토링
                .orElseThrow(() -> new PayException(ErrorCode.ACCOUNT_NOT_FOUND));
        validateOwner(member, account);
        return account;
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactions(String accountNumber, Member owner, Pageable pageable) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new PayException(ErrorCode.ACCOUNT_NOT_FOUND));
        validateOwner(owner, account);
        return transactionRepository
                .findAllByRecipientAccountOrRemitterAccountAndTransactionResultTypeOrderByCreatedAtDesc
                        (account, account, TransactionResultType.SUCCESS ,pageable);
    }

    private static void validateOwner(Member owner, Account account) {
        if (!owner.equals(account.getOwner())) {
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
