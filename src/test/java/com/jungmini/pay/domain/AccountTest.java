package com.jungmini.pay.domain;

import com.jungmini.pay.fixture.MemberFactory;
import com.jungmini.pay.type.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountTest {

    @Test
    @DisplayName("초기 생성되는 계좌 번호는 DEFAULT_ACCOUNT_NUMBER")
    void create_account_success_default_account_number() {
        Member owner = MemberFactory.member();

        Account newAccount = Account.from(owner, null);

        assertThat(newAccount.getAccountNumber()).isEqualTo(Account.DEFAULT_ACCOUNT_NUMBER);
        assertThat(newAccount.getBalance()).isEqualTo(0);
        assertThat(newAccount.getAccountStatus()).isEqualTo(AccountStatus.IN_USE);
        assertThat(newAccount.getMember()).isEqualTo(owner);
    }

    @Test
    @DisplayName("이후 생성되는 계좌 번호는 숫자가 1 더 크다")
    void create_account_success_accountNumber_increment() {
        Member owner = MemberFactory.member();
        Account account = Account.builder()
                .accountNumber("100000000000")
                .member(owner)
                .accountStatus(AccountStatus.IN_USE)
                .build();

        Account newAccount = Account.from(owner, account);

        assertThat(newAccount.getAccountNumber()).isEqualTo("100000000001");
        assertThat(newAccount.getBalance()).isEqualTo(0);
        assertThat(newAccount.getAccountStatus()).isEqualTo(AccountStatus.IN_USE);
        assertThat(newAccount.getMember()).isEqualTo(owner);
    }
}