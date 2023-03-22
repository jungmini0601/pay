package com.jungmini.pay.fixture;

import com.jungmini.pay.domain.Member;

public class MemberFactory {

    public static Member member() {
        return Member.builder()
                .email("test@test.com")
                .password("test")
                .name("test")
                .build();
    }
}
