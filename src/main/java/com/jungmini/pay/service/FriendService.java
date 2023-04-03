package com.jungmini.pay.service;

import com.jungmini.pay.domain.Friend;
import com.jungmini.pay.domain.FriendRequest;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.common.exception.ErrorCode;
import com.jungmini.pay.common.exception.PayException;
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

    /**
     * @Transactional(readOnlny=true)를 붙여야 하는가?
     *
     * readOnly 옵션을 주면 세션 플러시 모드 MANUAL로 변경
     * 플러시 발생 X 엔티티 등록 수정 삭제 동작 X
     * 변경감지를 위한 스냅샷 사용X 성능 증가
     *
     * MySQL8을 사용하면 REPETABLE READ 격리수준에서 PHANTOM READ 정합성 문제가 발생하지 않는다.
     * 한 트랜잭션 내부에서 레코드가 보였다 안보였다 하는 현상은 잔액조회시 한 트랜잭션 내에서 값이 바뀔 가능성이 존재한다.
     * 따라서 readOnly = true를 서비스 내부에서 계속해서 붙이는 것이 좋다는 것이 결론이다.
     *
     * 친구 요청 조회 기능은 트랜잭션이 필요한가?
     *
     * 일반 적인 경우라면 필요하지 않다고 생각한다.
     * 하지만 친구 도메인이 송금 기능과 엮여있는 만큼 높은 정합성이 보장된 데이터를 전달해 주기 위해 사용한다.
     */
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

    @Transactional
    public FriendRequest denyFriendRequest(final long friendRequestId) {
        FriendRequest friendRequest = friendRequestRepository.findById(friendRequestId)
                .orElseThrow(() -> new PayException(ErrorCode.BAD_REQUEST));

        friendRequestRepository.deleteById(friendRequestId);

        return friendRequest;
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

        if (friendRequestRepository.existsFriendRequestByRecipientAndRequester(requester, recipient)) {
            throw new PayException(ErrorCode.FRIENDS_REQUEST_EXISTS);
        }
    }

    private void checkExistsFriendFrom(Member recipient, Member requester) {
        if (friendRepository.existsFriendByRecipientAndRequester(recipient, requester)) {
            throw new PayException(ErrorCode.ALREADY_FRIENDS);
        }

        if (friendRepository.existsFriendByRecipientAndRequester(requester, recipient)) {
            throw new PayException(ErrorCode.ALREADY_FRIENDS);
        }
    }
}
