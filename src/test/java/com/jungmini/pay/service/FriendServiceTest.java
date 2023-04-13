package com.jungmini.pay.service;

import com.jungmini.pay.domain.Friend;
import com.jungmini.pay.domain.FriendRequest;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.common.exception.ErrorCode;
import com.jungmini.pay.common.exception.PayException;
import com.jungmini.pay.fixture.FriendRequestFactory;
import com.jungmini.pay.fixture.MemberFactory;
import com.jungmini.pay.repository.FriendRepository;
import com.jungmini.pay.repository.FriendRequestRepository;
import com.jungmini.pay.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FriendServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FriendRepository friendRepository;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @InjectMocks
    private FriendService friendService;

    @Test
    @DisplayName("친구 요청 성공")
    void create_friend_request_success() {
        FriendRequest friendRequest = FriendRequestFactory.friendRequest();

        given(memberRepository.findById(friendRequest.getRecipient().getEmail()))
                .willReturn(Optional.of(friendRequest.getRecipient()));

        given(memberRepository.findById(friendRequest.getRequester().getEmail()))
                .willReturn(Optional.of(friendRequest.getRequester()));

        given(friendRequestRepository.existsFriendRequestByRecipientAndRequester(any(), any()))
                .willReturn(false);

        given(friendRepository.existsFriendByRecipientAndRequester(any(), any()))
                .willReturn(false);

        given(friendRequestRepository.save(any())).willReturn(friendRequest);

        FriendRequest createdFriendRequest = friendService.requestFriend(friendRequest);

        assertThat(createdFriendRequest.getRequester().getEmail()).isEqualTo(friendRequest.getRequester().getEmail());
        assertThat(createdFriendRequest.getRecipient().getEmail()).isEqualTo(friendRequest.getRecipient().getEmail());
    }

    @Test
    @DisplayName("친구 요청 실패 - 이미 친구인 경우")
    void create_friend_request_fail_already_friends() {
        FriendRequest friendRequest = FriendRequestFactory.friendRequest();

        given(memberRepository.findById(friendRequest.getRecipient().getEmail()))
                .willReturn(Optional.of(friendRequest.getRecipient()));

        given(memberRepository.findById(friendRequest.getRequester().getEmail()))
                .willReturn(Optional.of(friendRequest.getRequester()));

        given(friendRepository.existsFriendByRecipientAndRequester(any(), any()))
                .willReturn(true);

        PayException payException = assertThrows(PayException.class, () -> {
            friendService.requestFriend(friendRequest);
        });

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.ALREADY_FRIENDS);
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.ALREADY_FRIENDS.getDescription());
    }

    @Test
    @DisplayName("친구 요청 실패 - 이미 친구인 경우 역 방향")
    void create_friend_request_fail_already_friends_reverse() {
        FriendRequest friendRequest = FriendRequestFactory.friendRequest();

        given(memberRepository.findById(friendRequest.getRecipient().getEmail()))
                .willReturn(Optional.of(friendRequest.getRecipient()));

        given(memberRepository.findById(friendRequest.getRequester().getEmail()))
                .willReturn(Optional.of(friendRequest.getRequester()));

        when(friendRepository.existsFriendByRecipientAndRequester(any(), any()))
                .thenReturn(false)
                .thenReturn(true);

        PayException payException = assertThrows(PayException.class, () -> {
            friendService.requestFriend(friendRequest);
        });

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.ALREADY_FRIENDS);
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.ALREADY_FRIENDS.getDescription());
    }

    @Test
    @DisplayName("친구 요청 실패 - 친구 요청이 존재하는 경우")
    void create_friend_request_fail_friend_request_exists() {
        FriendRequest friendRequest = FriendRequestFactory.friendRequest();

        given(memberRepository.findById(friendRequest.getRecipient().getEmail()))
                .willReturn(Optional.of(friendRequest.getRecipient()));

        given(memberRepository.findById(friendRequest.getRequester().getEmail()))
                .willReturn(Optional.of(friendRequest.getRequester()));

        given(friendRepository.existsFriendByRecipientAndRequester(any(), any()))
                .willReturn(false);

        given(friendRequestRepository.existsFriendRequestByRecipientAndRequester(any(), any()))
                .willReturn(true);

        PayException payException = assertThrows(PayException.class, () -> {
            friendService.requestFriend(friendRequest);
        });

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.FRIENDS_REQUEST_EXISTS);
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.FRIENDS_REQUEST_EXISTS.getDescription());
    }

    @Test
    @DisplayName("친구 요청 실패 - 친구 요청이 존재하는 경우 역방향")
    void create_friend_request_fail_friend_request_exists_reverse() {
        FriendRequest friendRequest = FriendRequestFactory.friendRequest();

        given(memberRepository.findById(friendRequest.getRecipient().getEmail()))
                .willReturn(Optional.of(friendRequest.getRecipient()));

        given(memberRepository.findById(friendRequest.getRequester().getEmail()))
                .willReturn(Optional.of(friendRequest.getRequester()));

        when(friendRequestRepository.existsFriendRequestByRecipientAndRequester(any(), any()))
                .thenReturn(true)
                .thenReturn(false);

        PayException payException = assertThrows(PayException.class, () -> {
            friendService.requestFriend(friendRequest);
        });

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.FRIENDS_REQUEST_EXISTS);
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.FRIENDS_REQUEST_EXISTS.getDescription());
    }

    @Test
    @DisplayName("친구 요청 실패 - 존재 하지 않는 회원인 경우")
    void create_friend_request_fail_member_not_found() {
        FriendRequest friendRequest = FriendRequestFactory.friendRequest();

        given(memberRepository.findById(friendRequest.getRecipient().getEmail()))
                .willReturn(Optional.empty());

        PayException payException = assertThrows(PayException.class, () -> {
            friendService.requestFriend(friendRequest);
        });

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getDescription());
    }


    @Test
    @DisplayName("친구 요청 실패 - 자기 자신에게 친구 요청 하는 경우")
    void create_friend_request_fail_self_request() {
        Member recipient = MemberFactory.member();
        Member requester = MemberFactory.member();
        FriendRequest friendRequest = FriendRequest.builder()
                .requester(requester)
                .recipient(recipient)
                .build();

        PayException payException = assertThrows(PayException.class, () -> {
            friendService.requestFriend(friendRequest);
        });

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.SELF_FRIEND_REQUEST);
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.SELF_FRIEND_REQUEST.getDescription());
    }

    @Test
    @DisplayName("친구 요청 수락 - 성공")
    void accept_friend_request_success() {
        FriendRequest friendRequest = FriendRequestFactory.friendRequest();
        Friend friend = Friend.from(friendRequest);

        given(friendRequestRepository.findById(any()))
                .willReturn(Optional.of(friendRequest));

        given(friendRepository.save(any()))
                .willReturn(friend);

        Friend savedFriend = friendService.acceptFriendRequest(1L);

        assertThat(savedFriend.getRequester()).isEqualTo(friend.getRequester());
        assertThat(savedFriend.getRecipient()).isEqualTo(friend.getRecipient());
    }

    @Test
    @DisplayName("친구 요청 수락 - 실패 친구 요청 못 찾는 경우")
    void accept_friend_request_fail_friend_request_not_found() {
        given(friendRequestRepository.findById(any()))
                .willReturn(Optional.empty());

        PayException payException = assertThrows(PayException.class,
                () -> friendService.acceptFriendRequest(1L));

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.BAD_REQUEST.getDescription());
    }

    @Test
    @DisplayName("친구 요청 거절 성공")
    void deny_friend_request_success() {
        FriendRequest friendRequest = FriendRequestFactory.friendRequest();
        Friend friend = Friend.from(friendRequest);

        given(friendRequestRepository.findById(any()))
                .willReturn(Optional.of(friendRequest));

        FriendRequest deletedFriendRequest = friendService.denyFriendRequest(1L);

        assertThat(deletedFriendRequest.getRequester()).isEqualTo(friend.getRequester());
        assertThat(deletedFriendRequest.getRecipient()).isEqualTo(friend.getRecipient());
    }

    @Test
    @DisplayName("친구 요청 실패 - 실패 친구 요청 못 찾는 경우")
    void deny_friend_request_fail_friend_request_not_found() {
        given(friendRequestRepository.findById(any()))
                .willReturn(Optional.empty());

        PayException payException = assertThrows(PayException.class,
                () -> friendService.denyFriendRequest(1L));

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.BAD_REQUEST.getDescription());
    }
}
