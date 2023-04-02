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

    /**
     * readOnly = true를 붙이면 스프링 프레임워크가 하이버네이트 세션 플러시 모드를 MANUL로 설정
     * 하이버네이트 세션 플러시 모드가 MANUAL일 경우, 강제로 플러시를 호출하지 않는한 플러시 발생 X
     * 엔티티 등록 수정 삭제 동작 X 변경 감지로 인한 스냅샷 사용 X 성능 UP
     *
     * 꼭 트랜잭션을 붙여야 하나? (한 트랜잭션 내에서 SELECT 쿼리 결과가 달라 질 수 있기 때문에 붙이는 것이 안전하다고 판단)
     * TODO 근거가 부족하지만 안정성을 위해 붙이는 것으로 결정 좀 더 조사가 필요함
     * JPA를 쓰면 영속성 컨택스트가 REPETABLE READ를 보장하지 않나?
     * PHANTOM READ는 어떻게 되나?
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
    }

    private void checkExistsFriendFrom(Member recipient, Member requester) {
        if (friendRepository.existsFriendByRecipientAndRequester(recipient, requester)) {
            throw new PayException(ErrorCode.ALREADY_FRIENDS);
        }
    }
}
