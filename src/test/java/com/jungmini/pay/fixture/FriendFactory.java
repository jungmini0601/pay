package com.jungmini.pay.fixture;

import com.jungmini.pay.domain.Friend;
import com.jungmini.pay.domain.Member;

public class FriendFactory {
    public static Friend friend() {
        Member requester = Member.builder()
                .email("requester@test.com")
                .name("requester")
                .password("tset")
                .build();

        Member recipient = Member.builder()
                .email("recipient@test.com")
                .name("recipent")
                .password("test")
                .build();

        return Friend.builder()
                .recipient(recipient)
                .requester(requester)
                .build();
    }

    public static Friend friendReverseDirection() {
        Member recipient = Member.builder()
                .email("requester@test.com")
                .name("requester")
                .password("tset")
                .build();

        Member requester = Member.builder()
                .email("recipient@test.com")
                .name("recipent")
                .password("test")
                .build();

        return Friend.builder()
                .recipient(recipient)
                .requester(requester)
                .build();
    }

    public static Friend from(Member requester, Member recipient) {
        return Friend.builder()
                .recipient(recipient)
                .requester(requester)
                .build();
    }
}
