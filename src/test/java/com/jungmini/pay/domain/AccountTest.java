package com.jungmini.pay.domain;

import com.jungmini.pay.common.exception.ErrorCode;
import com.jungmini.pay.common.exception.PayException;
import com.jungmini.pay.fixture.AccountFactory;
import com.jungmini.pay.fixture.MemberFactory;
import com.jungmini.pay.domain.type.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountTest {

    @Test
    @DisplayName("초기 생성되는 계좌 번호는 DEFAULT_ACCOUNT_NUMBER")
    void create_account_success_default_account_number() {
        Member owner = MemberFactory.member();

        Account newAccount = Account.from(owner, null);

        assertThat(newAccount.getAccountNumber()).isEqualTo(Account.DEFAULT_ACCOUNT_NUMBER);
        assertThat(newAccount.getBalance()).isEqualTo(0);
        assertThat(newAccount.getAccountStatus()).isEqualTo(AccountStatus.IN_USE);
        assertThat(newAccount.getOwner()).isEqualTo(owner);
    }

    @Test
    @DisplayName("이후 생성되는 계좌 번호는 숫자가 1 더 크다")
    void create_account_success_accountNumber_increment() {
        Member owner = MemberFactory.member();
        Account account = Account.builder()
                .accountNumber("100000000000")
                .owner(owner)
                .accountStatus(AccountStatus.IN_USE)
                .build();

        Account newAccount = Account.from(owner, account);

        assertThat(newAccount.getAccountNumber()).isEqualTo("100000000001");
        assertThat(newAccount.getBalance()).isEqualTo(0);
        assertThat(newAccount.getAccountStatus()).isEqualTo(AccountStatus.IN_USE);
        assertThat(newAccount.getOwner()).isEqualTo(owner);
    }

    @Test
    @DisplayName("계좌 충전 성공")
    void charge_point_success() {
        int amount = 100000;
        Member owner = MemberFactory.member();
        Account account = AccountFactory.accountFrom(owner);

        account.chargePoint(amount, owner);

        assertThat(account.getBalance()).isEqualTo(amount);
    }

    @Test
    @DisplayName("계좌 충전 실패 - 계좌 소유주 아님")
    void charge_point_fail_requester_is_not_owner() {
        int amount = 100000;
        Member owner = MemberFactory.member();
        Member requester = Member.builder().email("requester").build();
        Account account = AccountFactory.accountFrom(owner);

        PayException payException = assertThrows(PayException.class,
                () -> account.chargePoint(amount, requester));

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.REQUESTER_IS_NOT_OWNER.toString());
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.REQUESTER_IS_NOT_OWNER.getDescription());
    }
}