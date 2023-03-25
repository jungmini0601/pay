package com.jungmini.pay.fixture;

import com.jungmini.pay.domain.FriendRequest;
import com.jungmini.pay.domain.Member;

public class FriendRequestFactory {

    public static FriendRequest friendRequest() {
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

        return FriendRequest.from(requester, recipient);
    }
}
