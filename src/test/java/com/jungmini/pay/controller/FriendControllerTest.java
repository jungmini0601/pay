package com.jungmini.pay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jungmini.pay.controller.dto.FriendDTO;
import com.jungmini.pay.domain.Friend;
import com.jungmini.pay.domain.FriendRequest;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.exception.ErrorCode;
import com.jungmini.pay.fixture.FriendFactory;
import com.jungmini.pay.fixture.FriendRequestFactory;
import com.jungmini.pay.fixture.MemberFactory;
import com.jungmini.pay.repository.FriendRepository;
import com.jungmini.pay.service.FriendService;
import com.jungmini.pay.service.MemberService;
import com.jungmini.pay.service.TokenService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest
public class FriendControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberService memberService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private FriendService friendService;

    @Autowired
    private FriendRepository friendRepository;

    @Test
    @DisplayName("통합 테스트 - 친구 요청 성공")
    void createFriendRequest_success() throws Exception {
        FriendRequest friendRequest = FriendRequestFactory.friendRequest();
        Member requester = friendRequest.getRequester();
        Member recipient = friendRequest.getRecipient();
        memberService.signUp(requester);
        memberService.signUp(recipient);
        String token = tokenService.generateToken(friendRequest.getRequester().getEmail());

        mvc.perform(
                post("/friends/request")
                    .header("Auth",token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(friendRequest.getRecipient().getEmail())))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.message").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("통합 테스트 실패 - 친구 요청 토큰 X")
    void createFriendRequest_fail_UnAuthorized() throws Exception {
        FriendRequest friendRequest = FriendRequestFactory.friendRequest();

        mvc.perform(
                post("/friends/request")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(friendRequest.getRecipient().getEmail())))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.UN_AUTHORIZED.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UN_AUTHORIZED.getDescription()))
                .andDo(print());
    }

    @Test
    @DisplayName("통합 테스트 실패 - 친구 요청이 존재하는 경우")
    void createFriendRequest_fail_request_exists() throws Exception {
        FriendRequest friendRequest = FriendRequestFactory.friendRequest();
        Member requester = friendRequest.getRequester();
        Member recipient = friendRequest.getRecipient();
        memberService.signUp(requester);
        memberService.signUp(recipient);
        friendService.requestFriend(friendRequest);
        String token = tokenService.generateToken(friendRequest.getRequester().getEmail());

        mvc.perform(
                post("/friends/request")
                    .header("Auth",token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(friendRequest.getRecipient().getEmail())))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.FRIENDS_REQUEST_EXISTS.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.FRIENDS_REQUEST_EXISTS.getDescription()))
                .andDo(print());
    }

    @Test
    @DisplayName("통합 테스트 실패 - 이미 친구인경우")
    void createFriendRequest_fail_already_friends() throws Exception {
        Friend friend = FriendFactory.friend();
        Member requester = friend.getRequester();
        Member recipient = friend.getRecipient();
        memberService.signUp(requester);
        memberService.signUp(recipient);
        friendRepository.save(friend);
        String token = tokenService.generateToken(requester.getEmail());

        mvc.perform(
                post("/friends/request")
                    .header("Auth",token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(recipient.getEmail())))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.ALREADY_FRIENDS.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_FRIENDS.getDescription()))
                .andDo(print());
    }

    @Test
    @DisplayName("통합 테스트 실패 - 입력값 검증 실패")
    void createFriendRequest_fail_bad_request() throws Exception {
        Member member = MemberFactory.member();
        FriendDTO.CreateFriendRequest request = FriendDTO.CreateFriendRequest.builder().build();
        memberService.signUp(member);
        String token = tokenService.generateToken(member.getEmail());

        mvc.perform(
                post("/friends/request")
                    .header("Auth", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.BAD_REQUEST.toString()))
                .andExpect(jsonPath("$.errorFields").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("통합 테스트 성공 - 친구 요청 조회")
    void getFriendRequests_success() throws Exception {
        FriendRequest friendRequest = FriendRequestFactory.friendRequest();
        Member requester = friendRequest.getRequester();
        Member recipient = friendRequest.getRecipient();
        memberService.signUp(requester);
        memberService.signUp(recipient);
        friendService.requestFriend(friendRequest);
        String token = tokenService.generateToken(friendRequest.getRecipient().getEmail());

        mvc.perform(
                    get("/friends/request")
                    .header("Auth", token))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
    }

    @Test
    @DisplayName("통합 테스트 성공 - 친구 요청 수락 성공")
    void acceptFriendRequest_success() throws Exception {
        FriendRequest friendRequest = FriendRequestFactory.friendRequest();
        Member requester = friendRequest.getRequester();
        Member recipient = friendRequest.getRecipient();
        memberService.signUp(requester);
        memberService.signUp(recipient);
        FriendRequest createdRequest = friendService.requestFriend(friendRequest);

        String token = tokenService.generateToken(recipient.getEmail());

        mvc.perform(post("/friends/requests/accept/" + createdRequest.getId())
                    .header("Auth", token))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.message").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("통합 테스트 성공 - 친구 요청 토큰 X")
    void acceptFriendRequest_fail_UnAuthorized() throws Exception {
        mvc.perform(post("/friends/requests/accept/1"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.UN_AUTHORIZED.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UN_AUTHORIZED.getDescription()))
                .andDo(print());
    }

    @Test
    @DisplayName("통합 테스트 성공 - 친구 요청 거절 성공")
    void denyFriendRequest_success() throws Exception {
        FriendRequest friendRequest = FriendRequestFactory.friendRequest();
        Member requester = friendRequest.getRequester();
        Member recipient = friendRequest.getRecipient();
        memberService.signUp(requester);
        memberService.signUp(recipient);
        FriendRequest createdRequest = friendService.requestFriend(friendRequest);

        String token = tokenService.generateToken(recipient.getEmail());

        mvc.perform(post("/friends/requests/deny/" + createdRequest.getId())
                        .header("Auth", token))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.message").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("통합 테스트 성공 - 친구 요청 거절 실패 토큰 X")
    void denyFriendRequest_fail() throws Exception {
        mvc.perform(post("/friends/requests/deny/1"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.UN_AUTHORIZED.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UN_AUTHORIZED.getDescription()))
                .andDo(print());
    }
}