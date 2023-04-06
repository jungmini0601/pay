package com.jungmini.pay.controller;

import com.jungmini.pay.domain.Friend;
import com.jungmini.pay.domain.FriendRequest;
import com.jungmini.pay.domain.Member;

import com.jungmini.pay.common.resolover.SigninMember;
import com.jungmini.pay.controller.dto.FriendDTO;
import com.jungmini.pay.service.FriendService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/friends/request")
    public ResponseEntity<FriendDTO.CreateFriendResponse> createFriendRequest(
            @RequestBody @Valid FriendDTO.CreateFriendRequest createFriendRequest,
            @SigninMember Member signinMember
    ) {

        Member recipient = Member
                .builder()
                .email(createFriendRequest.getEmail())
                .build();

        FriendRequest friendRequest = FriendRequest
                .builder()
                .requester(signinMember)
                .recipient(recipient)
                .build();

        friendService.requestFriend(friendRequest);

        return ResponseEntity.ok(FriendDTO.CreateFriendResponse.builder()
                .message(String.format("%s님에게 친구 요청을 보냈습니다.", createFriendRequest.getEmail()))
                .build());
    }

    @PostMapping("/friends/requests/accept/{id}")
    public ResponseEntity<FriendDTO.AcceptFriendRequestResponse> acceptFriendRequest(
            @PathVariable long id,
            @SigninMember Member signinMember
    ) {
        Friend friend = friendService.acceptFriendRequest(id);
        return ResponseEntity.ok(FriendDTO.AcceptFriendRequestResponse
                .builder()
                .message(String.format("%s님의 친구 요청 수락 완료", friend.getRequester()))
                .build());
    }
    @PostMapping("/friends/requests/deny/{id}")
    public ResponseEntity<FriendDTO.DenyFriendRequestResponse> denyFriendRequest(
            @PathVariable long id,
            @SigninMember Member signinMember
    ) {
        FriendRequest friendRequest = friendService.denyFriendRequest(id);
        return ResponseEntity.ok(FriendDTO.DenyFriendRequestResponse
                .builder()
                .message(String.format("%s님의 친구 요청 거절 완료", friendRequest.getRequester().getEmail()))
                .build());
    }


    @GetMapping("/friends/request")
    public ResponseEntity<List<FriendDTO.FindFriendRequestResponse>> findFriendRequests(
            Pageable pageable,
            @SigninMember Member signinMember
    ) {
        List<FriendDTO.FindFriendRequestResponse> requesters =
                friendService.findRequests(pageable, signinMember).stream()
                .map(FriendDTO.FindFriendRequestResponse::from)
                .toList();

        return ResponseEntity.ok(requesters);
    }
}
