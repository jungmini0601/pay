package com.jungmini.pay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.exception.ErrorCode;
import com.jungmini.pay.fixture.MemberFactory;
import com.jungmini.pay.service.MemberService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest
class MemberControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberService memberService;

    @DisplayName("통합테스트 - 회원가입 성공")
    @Test
    void signup_success() throws Exception {
        Member member = MemberFactory.member();

        mvc.perform(
                post("/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(member)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.name").value(member.getName()))
                .andExpect(jsonPath("$.email").value(member.getEmail()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andDo(print());
    }

    @DisplayName("통합테스트 - 회원가입 실패 400번 입력 값 에러")
    @Test
    void signup_badRequest() throws Exception {
        Member member = Member.builder().build();

        mvc.perform(
                post("/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(member)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.BAD_REQUEST.toString()))
                .andExpect(jsonPath("$.errorFields").exists())
                .andDo(print());
    }

    @DisplayName("통합테스트 - 회원가입 실패 중복된 이메일")
    @Test
    void signup_duplicated_member() throws Exception {
        Member member = MemberFactory.member();
        memberService.signUp(member);

        mvc.perform(
                post("/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(member)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.MEMBER_DUPLICATED.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_DUPLICATED.getDescription()))
                .andDo(print());
    }

    @DisplayName("통합테스트 - 로그인 성공")
    @Test
    void  signin_success() throws Exception {
        Member member = MemberFactory.member();
        memberService.signUp(member);

        mvc.perform(
                post("/members/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(member)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(header().exists("Auth"))
                .andDo(print());
    }

    @DisplayName("통합테스트 - 로그인 실패 비밀번호 불일치")
    @Test
    void  signin_fail_password_mismatch() throws Exception {
        Member member = MemberFactory.member();
        memberService.signUp(member);

        Member memberRequest = Member.builder()
                .email(member.getEmail())
                .password("password_mismatch")
                .build();

        mvc.perform(
                post("/members/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(memberRequest)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.PASSWORD_MISMATCH.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.PASSWORD_MISMATCH.getDescription()))
                .andDo(print());
    }

    @DisplayName("통합테스트 - 로그인 실패 존재하지 않는 회원")
    @Test
    void  signin_fail_member_not_found() throws Exception {
        Member member = MemberFactory.member();

        mvc.perform(
                post("/members/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(member)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.BAD_REQUEST.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.BAD_REQUEST.getDescription()))
                .andDo(print());
    }

    @DisplayName("통합테스트 - 로그인 실패 400번 입력 값 에러")
    @Test
    void signin_badRequest() throws Exception {
        Member member = Member.builder().build();

        mvc.perform(
                post("/members/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(member)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.BAD_REQUEST.toString()))
                .andExpect(jsonPath("$.errorFields").exists())
                .andDo(print());
    }
}