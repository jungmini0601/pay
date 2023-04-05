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

    public static Member memberFrom(String email) {
        return Member.builder()
                .email(email)
                .build();
    }

    public static Member memberFrom(String email, String password) {
        return Member.builder()
                .email(email)
                .password(password)
                .build();
    }
}
