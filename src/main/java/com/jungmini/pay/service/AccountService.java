package com.jungmini.pay.service;

import com.jungmini.pay.domain.Account;
import com.jungmini.pay.domain.AccountNumber;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.common.exception.ErrorCode;
import com.jungmini.pay.common.exception.PayException;
import com.jungmini.pay.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jungmini.pay.domain.Account.MAX_ACCOUNT_SIZE;

@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;

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

    private void validateAccountSize(Member owner) {
        int accountCount = accountRepository.countByOwner(owner);

        if (accountCount >= MAX_ACCOUNT_SIZE) {
            throw new PayException(ErrorCode.ACCOUNT_SIZE_EXCEED);
        }
    }
}
