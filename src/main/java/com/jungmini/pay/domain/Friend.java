package com.jungmini.pay.domain;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Friend extends BaseTimeEntity {

    @Id
    @Column(name = "friend_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "friend_requester_email")
    private Member requester;

    @ManyToOne
    @JoinColumn(name = "friend_recipient_email")
    private Member recipient;

    public static Friend from(FriendRequest friendRequest) {
        return Friend.builder()
                .requester(friendRequest.getRequester())
                .recipient(friendRequest.getRecipient())
                .build();
    }

}
