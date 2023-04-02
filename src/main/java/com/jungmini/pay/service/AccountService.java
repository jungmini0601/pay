package com.jungmini.pay.service;

import com.jungmini.pay.domain.Account;
import com.jungmini.pay.domain.AccountNumber;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.exception.ErrorCode;
import com.jungmini.pay.exception.PayException;
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

    private void validateAccountSize(Member owner) {
        int accountCount = accountRepository.countByMember(owner);

        if (accountCount >= MAX_ACCOUNT_SIZE) {
            throw new PayException(ErrorCode.ACCOUNT_SIZE_EXCEED);
        }
    }
}
