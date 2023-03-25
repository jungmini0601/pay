package com.jungmini.pay.domain;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class FriendRequest {
    @Id
    @Column(name = "friend_request_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "requester_email")
    private Member requester;

    @ManyToOne
    @JoinColumn(name = "recipient_email")
    private Member recipient;

    public static FriendRequest from(Member requester, Member recipient) {
        return FriendRequest.builder()
                .requester(requester)
                .recipient(recipient)
                .build();
    }
}
