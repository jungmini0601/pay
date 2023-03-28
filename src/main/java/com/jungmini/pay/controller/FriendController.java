package com.jungmini.pay.controller;

import com.jungmini.pay.common.resolover.SigninMember;
import com.jungmini.pay.controller.dto.FriendDTO;
import com.jungmini.pay.domain.FriendRequest;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.service.FriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/friends/request")
    public ResponseEntity<List<FriendDTO.FindFriendRequestResponse>> findFriendRequests(
            Pageable pageable,
            @SigninMember Member signinMember
    ) {
        List<FriendDTO.FindFriendRequestResponse> requesters =
                friendService.findRequests(pageable, signinMember).stream()
                .map(friendRequest -> FriendDTO.FindFriendRequestResponse.from(friendRequest.getRequester()))
                .toList();

        return ResponseEntity.ok(requesters);
    }
}
