package com.jungmini.pay.service;

import com.jungmini.pay.domain.FriendRequest;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.exception.ErrorCode;
import com.jungmini.pay.exception.PayException;
import com.jungmini.pay.repository.FriendRepository;
import com.jungmini.pay.repository.FriendRequestRepository;
import com.jungmini.pay.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FriendService {

    private final MemberRepository memberRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendRepository friendRepository;

    @Transactional
    public FriendRequest requestFriend(FriendRequest request) {
        Member recipient = memberRepository.findById(request.getRecipient().getEmail())
                .orElseThrow(() -> new PayException(ErrorCode.BAD_REQUEST));

        Member requester = memberRepository.findById(request.getRequester().getEmail())
                .orElseThrow(() -> new PayException(ErrorCode.BAD_REQUEST));

        checkExistsFriendFrom(recipient, requester);
        checkExistsFriendRequestFrom(recipient, requester);

        return friendRequestRepository.save(FriendRequest.from(requester, recipient));
    }

    private void checkExistsFriendRequestFrom(Member recipient, Member requester) {
        if (friendRequestRepository.existsFriendRequestByRecipientAndRequester(recipient, requester)) {
            throw new PayException(ErrorCode.FRIENDS_REQUEST_EXISTS);
        }
    }

    private void checkExistsFriendFrom(Member recipient, Member requester) {
        if (friendRepository.existsFriendByRecipientAndRequester(recipient, requester)) {
            throw new PayException(ErrorCode.ALREADY_FRIENDS);
        }
    }
}
