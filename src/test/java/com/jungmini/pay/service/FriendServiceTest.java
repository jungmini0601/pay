package com.jungmini.pay.service;

import com.jungmini.pay.domain.FriendRequest;
import com.jungmini.pay.exception.ErrorCode;
import com.jungmini.pay.exception.PayException;
import com.jungmini.pay.fixture.FriendRequestFactory;
import com.jungmini.pay.repository.FriendRepository;
import com.jungmini.pay.repository.FriendRequestRepository;
import com.jungmini.pay.repository.MemberRepository;
import org.assertj.core.api.Assertions;
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
    void createFriendRequest_success() {
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
    void createFriendRequest_fail_already_friends() {
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

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.ALREADY_FRIENDS.toString());
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.ALREADY_FRIENDS.getDescription().toString());
    }

    @Test
    @DisplayName("친구 요청 실패 - 친구 요청이 존재하는 경우")
    void createFriendRequest_fail_friend_request_exists() {
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

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.FRIENDS_REQUEST_EXISTS.toString());
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.FRIENDS_REQUEST_EXISTS.getDescription().toString());
    }

    @Test
    @DisplayName("친구 요청 실패 - 존재 하지 않는 친구인 경우")
    void createFriendRequest_member_not_found() {
        FriendRequest friendRequest = FriendRequestFactory.friendRequest();

        given(memberRepository.findById(friendRequest.getRecipient().getEmail()))
                .willReturn(Optional.empty());

        PayException payException = assertThrows(PayException.class, () -> {
            friendService.requestFriend(friendRequest);
        });

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST.toString());
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.BAD_REQUEST.getDescription().toString());
    }
}
