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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}