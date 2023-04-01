package com.jungmini.pay.service;

import com.jungmini.pay.domain.Friend;
import com.jungmini.pay.domain.FriendRequest;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.exception.ErrorCode;
import com.jungmini.pay.exception.PayException;
import com.jungmini.pay.repository.FriendRepository;
import com.jungmini.pay.repository.FriendRequestRepository;
import com.jungmini.pay.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class FriendService {

    private final MemberRepository memberRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendRepository friendRepository;

    @Transactional
    public FriendRequest requestFriend(FriendRequest request) {
        validationFriendRequest(request);

        Member recipient = memberRepository.findById(request.getRecipient().getEmail())
                .orElseThrow(() -> new PayException(ErrorCode.MEMBER_NOT_FOUND));

        Member requester = memberRepository.findById(request.getRequester().getEmail())
                .orElseThrow(() -> new PayException(ErrorCode.MEMBER_NOT_FOUND));

        checkExistsFriendFrom(recipient, requester);
        checkExistsFriendRequestFrom(recipient, requester);

        return friendRequestRepository.save(FriendRequest.from(requester, recipient));
    }

    @Transactional(readOnly = true)
    public List<FriendRequest> findRequests(Pageable pageable, Member recipient) {
        return friendRequestRepository
                .findFriendRequestByRecipientOrderByCreatedAtDesc(recipient, pageable);
    }

    @Transactional
    public Friend acceptFriendRequest(final long friendRequestId) {
        FriendRequest friendRequest = friendRequestRepository.findById(friendRequestId)
                .orElseThrow(() -> new PayException(ErrorCode.BAD_REQUEST));

        Friend savedFriend = friendRepository.save(Friend.from(friendRequest));
        friendRequestRepository.deleteById(friendRequestId);

        return savedFriend;
    }

    private void validationFriendRequest(FriendRequest request) {
        if (request.getRequester().getEmail().equals(request.getRecipient().getEmail())) {
            throw new PayException(ErrorCode.SELF_FRIEND_REQUEST);
        }
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
