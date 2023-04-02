package com.jungmini.pay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jungmini.pay.domain.Account;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.exception.ErrorCode;
import com.jungmini.pay.fixture.AccountFactory;
import com.jungmini.pay.fixture.MemberFactory;
import com.jungmini.pay.service.AccountService;
import com.jungmini.pay.service.MemberService;
import com.jungmini.pay.service.TokenService;
import com.jungmini.pay.type.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest
public class AccountControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberService memberService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TokenService tokenService;

    @DisplayName("통합테스트 - 첫 번째 계좌 생성")
    @Test
    void create_first_account_success() throws Exception {
        Member member = MemberFactory.member();
        memberService.signUp(member);
        String token = tokenService.generateToken(member.getEmail());

        mvc.perform(
                        post("/accounts")
                                .header("Auth", token))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.accountNumber").value(Account.DEFAULT_ACCOUNT_NUMBER))
                .andExpect(jsonPath("$.accountStatus").value(AccountStatus.IN_USE.toString()))
                .andDo(print());
    }

    @DisplayName("통합테스트 - 두 번째 계좌 생성")
    @Test
    void create_second_account_success() throws Exception {
        Member member = MemberFactory.member();
        memberService.signUp(member);
        accountService.createAccount(member);
        String token = tokenService.generateToken(member.getEmail());

        Account secondAccount = AccountFactory.secondAccount();

        mvc.perform(
                        post("/accounts")
                                .header("Auth", token))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.accountNumber").value(secondAccount.getAccountNumber()))
                .andExpect(jsonPath("$.accountStatus").value(AccountStatus.IN_USE.toString()))
                .andDo(print());
    }

    @DisplayName("통합테스트 계좌 생성 실패 - 최대 계좌 생성 개수 초과")
    @Test
    void create_account_fail_exceed_max_account() throws Exception {
        Member member = MemberFactory.member();

        memberService.signUp(member);

        for (int i = 0; i < Account.MAX_ACCOUNT_SIZE; i++) {
            accountService.createAccount(member);
        }

        String token = tokenService.generateToken(member.getEmail());

        mvc.perform(
                        post("/accounts")
                                .header("Auth", token))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.ACCOUNT_SIZE_EXCEED.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.ACCOUNT_SIZE_EXCEED.getDescription()))
                .andDo(print());
    }

    @DisplayName("통합테스트 계좌 생성 실패 - 로그인 하지 않은 유저")
    @Test
    void create_account_fail_without_token() throws Exception {
        mvc.perform(
                        post("/accounts"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.UN_AUTHORIZED.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UN_AUTHORIZED.getDescription()))
                .andDo(print());
    }
}
