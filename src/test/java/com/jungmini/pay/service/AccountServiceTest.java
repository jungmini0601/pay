package com.jungmini.pay.service;

import com.jungmini.pay.domain.Account;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.common.exception.ErrorCode;
import com.jungmini.pay.common.exception.PayException;
import com.jungmini.pay.fixture.AccountFactory;
import com.jungmini.pay.fixture.MemberFactory;
import com.jungmini.pay.repository.AccountRepository;
import com.jungmini.pay.domain.type.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.jungmini.pay.domain.Account.MAX_ACCOUNT_SIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("계좌 생성 성공")
    void create_account_success() {
        Member owner = MemberFactory.member();
        Account account = AccountFactory.account();

        given(accountRepository.findFirstByOrderByCreatedAtDesc())
                .willReturn(Optional.empty());

        given(accountRepository.save(any()))
                .willReturn(account);

        Account createdAccount = accountService.createAccount(owner);

        assertThat(createdAccount.getAccountNumber()).isEqualTo(Account.DEFAULT_ACCOUNT_NUMBER);
        assertThat(createdAccount.getBalance()).isEqualTo(0);
        assertThat(createdAccount.getAccountStatus()).isEqualTo(AccountStatus.IN_USE);
        assertThat(createdAccount.getOwner().getEmail()).isEqualTo(owner.getEmail());
    }

    @Test
    @DisplayName("계좌 생성 실패 - 최대 계좌 생성 개수 초과")
    void create_account_fail_account_size_exceed() {
        Member owner = MemberFactory.member();

        given(accountRepository.countByOwner(any()))
                .willReturn(MAX_ACCOUNT_SIZE);

        PayException payException = assertThrows(PayException.class, () -> {
            accountService.createAccount(owner);
        });

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_SIZE_EXCEED.toString());
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.ACCOUNT_SIZE_EXCEED.getDescription());
    }

    @Test
    @DisplayName("계좌 충전 성공")
    void charge_point_success() {
        Member owner = MemberFactory.member();
        Account account = AccountFactory.accountFrom(owner);
        int amount = 100000;

        given(accountRepository.findById(any()))
                .willReturn(Optional.of(account));

        Account processedAccount = accountService.chargePoint(amount, account, owner);

        assertThat(processedAccount.getBalance()).isEqualTo(amount);
    }

    @Test
    @DisplayName("계좌 충전 실패 - 계좌 못 찾은 경우")
    void charge_point_fail_account_not_found() {
        Member owner = MemberFactory.member();
        Account account = AccountFactory.accountFrom(owner);
        int amount = 100000;

        given(accountRepository.findById(any()))
                .willReturn(Optional.empty());

        PayException payException = assertThrows(PayException.class, () -> {
            accountService.chargePoint(amount, account, owner);
        });

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND.toString());
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND.getDescription());
    }

}