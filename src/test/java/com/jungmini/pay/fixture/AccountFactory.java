package com.jungmini.pay.fixture;

import com.jungmini.pay.domain.Account;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.type.AccountStatus;

public class AccountFactory {

    public static Account account() {
        Member owner = MemberFactory.member();

        return Account.builder()
                .balance(0)
                .accountNumber(Account.DEFAULT_ACCOUNT_NUMBER)
                .accountStatus(AccountStatus.IN_USE)
                .member(owner)
                .build();
    }

    public static Account secondAccount() {
        Member owner = MemberFactory.member();
        return Account.from(owner, account());
    }
}
