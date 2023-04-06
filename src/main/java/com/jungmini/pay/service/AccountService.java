package com.jungmini.pay.service;

import com.jungmini.pay.domain.Account;
import com.jungmini.pay.domain.AccountNumber;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.domain.Transaction;
import com.jungmini.pay.domain.type.TransactionResultType;
import com.jungmini.pay.domain.type.TransactionType;

import com.jungmini.pay.common.exception.ErrorCode;
import com.jungmini.pay.common.exception.PayException;
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

    /**
     * @param owner 계좌 소유주
     * @return 생성된 계좌
     */
    @Transactional
    public Account createAccount(Member owner) {
        validateAccountSize(owner);
        AccountNumber recentAccountNumber = findRecentAccount();
        Account newAccount = Account.from(owner, recentAccountNumber);
        return accountRepository.save(newAccount);
    }

    /**
     * @param amount 충전 금액
     * @param accountNumber 충전할 계좌 번호
     * @param requester 요청자
     * @return 충전된 계좌 정보
     */
    @Transactional
    public Account chargePoint(int amount, AccountNumber accountNumber, Member requester) {
        Account account = accountRepository.findById(accountNumber.getAccountNumber())
                .orElseThrow(() -> new PayException(ErrorCode.ACCOUNT_NOT_FOUND));
        account.chargePoint(amount, requester);
        return account;
    }

    /**
     * 이 기능은 거래 생성에 실패하더라도 실패 정보를 DB에 저장해야 한다.
     * @param transactionRequest 송금계좌, 수신계좌, 송금액
     * @param remitter 송금자
     * @return 생성된 거래 정보
     */
    @Transactional
    public Transaction remit(Transaction transactionRequest, Member remitter) {
        try {
            Account recipientAccount = findAccount(transactionRequest.getRecipientAccount().getAccountNumber());
            Account remitterAccount = findAccount(transactionRequest.getRemitterAccount().getAccountNumber());
            validateOwner(remitter, remitterAccount);
            validateFriendRelation(remitter, recipientAccount);
            transactionRequest.successTransaction(recipientAccount, remitterAccount, TransactionType.REMIT);
            return transactionRepository.save(transactionRequest);
        } catch (PayException e) {
            saveFailTransaction(transactionRequest, e);
            throw e;
        }
    }

    /**
     * @param accountNumber 계좌 번호
     * @param owner 계좌 소유주 정보
     * @return 계좌 정보
     */
    @Transactional(readOnly = true)
    public Account getAccountInfo(String accountNumber, Member owner) {
        Account account = findAccount(accountNumber);
        validateOwner(owner, account);
        return account;
    }

    /**
     * @param accountNumber 계좌 정보
     * @param owner 소유주 정보
     * @param pageable 페이징 데이터
     * @return 거래 내용 리스트
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactions(String accountNumber, Member owner, Pageable pageable) {
        Account account = findAccount(accountNumber);
        validateOwner(owner, account);
        return transactionRepository
                .findAllByRecipientAccountOrRemitterAccountAndTransactionResultTypeOrderByCreatedAtDesc
                        (account, account, TransactionResultType.SUCCESS ,pageable);
    }

    private void saveFailTransaction(Transaction transactionRequest, PayException e) {
        if (!e.getErrorCode().equals("ACCOUNT_NOT_FOUND")) { // TODO 시간 없음 나중에 고치기
            Account recipientAccount = findAccount(transactionRequest.getRecipientAccount().getAccountNumber());
            Account remitterAccount = findAccount(transactionRequest.getRemitterAccount().getAccountNumber());
            transactionRequest.failTransaction(recipientAccount, remitterAccount, TransactionType.REMIT);
            transactionRepository.save(transactionRequest);
        }
    }

    private Account findAccount(String accountNumber) {
        return accountRepository.findById(accountNumber)
                .orElseThrow(() -> new PayException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    private static void validateOwner(Member owner, Account account) {
        if (!owner.equals(account.getOwner())) {
            throw new PayException(ErrorCode.REQUESTER_IS_NOT_OWNER);
        }
    }

    private void validateFriendRelation(Member remitter, Account recipientAccount) {
        boolean relation1 = findRelation(remitter, recipientAccount.getOwner());
        boolean relation2 = findRelation(recipientAccount.getOwner(), remitter);

        if (!relation1 && !relation2) {
            throw new PayException(ErrorCode.NOT_FRIENDS);
        }
    }

    private boolean findRelation(Member member1, Member member2) {
        return friendRepository.existsFriendByRecipientAndRequester(member1, member2);
    }

    private void validateAccountSize(Member owner) {
        int accountCount = accountRepository.countByOwner(owner);

        if (accountCount >= MAX_ACCOUNT_SIZE) {
            throw new PayException(ErrorCode.ACCOUNT_SIZE_EXCEED);
        }
    }

    private AccountNumber findRecentAccount() {
        return accountRepository.findFirstByOrderByCreatedAtDesc().orElse(null);
    }
}
