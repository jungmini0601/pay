package com.jungmini.pay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jungmini.pay.controller.dto.AccountDTO;
import com.jungmini.pay.domain.Account;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.common.exception.ErrorCode;
import com.jungmini.pay.fixture.AccountFactory;
import com.jungmini.pay.fixture.MemberFactory;
import com.jungmini.pay.service.AccountService;
import com.jungmini.pay.service.MemberService;
import com.jungmini.pay.service.TokenService;
import com.jungmini.pay.domain.type.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

    @DisplayName("통합테스트 계좌 생성 성공 - 첫 번째 계좌 생성")
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

    @DisplayName("통합테스트 계좌 생성 성공 - 두 번째 계좌 생성")
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

    @DisplayName("통합테스트 포인트 충전 성공 - 1만원 충전 하는 경우")
    @Test
    void charge_point_success_amount_10000() throws Exception {
        Member owner = MemberFactory.member();
        memberService.signUp(owner);
        Account account = accountService.createAccount(owner);
        int amount = 10000;
        String token = tokenService.generateToken(owner.getEmail());

        AccountDTO.ChargePointRequest request = AccountDTO.ChargePointRequest.builder()
                .amount(amount)
                .accountNumber(account.getAccountNumber())
                .build();

        mvc.perform(
                        post("/accounts/points")
                                .header("Auth", token)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.balance").value(amount))
                .andExpect(jsonPath("$.accountNumber").value(account.getAccountNumber()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andDo(print());
    }

    @DisplayName("통합테스트 포인트 충전 성공 - 200만원 충전 하는 경우")
    @Test
    void charge_point_success_amount_2000000() throws Exception {
        Member owner = MemberFactory.member();
        memberService.signUp(owner);
        Account account = accountService.createAccount(owner);
        int amount = 2000000;
        String token = tokenService.generateToken(owner.getEmail());

        AccountDTO.ChargePointRequest request = AccountDTO.ChargePointRequest.builder()
                .amount(amount)
                .accountNumber(account.getAccountNumber())
                .build();

        mvc.perform(
                        post("/accounts/points")
                                .header("Auth", token)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.balance").value(amount))
                .andExpect(jsonPath("$.accountNumber").value(account.getAccountNumber()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andDo(print());
    }

    @DisplayName("통합테스트 포인트 충전 실패 - 1만원 보다 작은 경우")
    @Test
    void charge_point_fail_less_than_10000() throws Exception{
        Member owner = MemberFactory.member();
        memberService.signUp(owner);
        Account account = accountService.createAccount(owner);
        int amount = 999;
        String token = tokenService.generateToken(owner.getEmail());

        AccountDTO.ChargePointRequest request = AccountDTO.ChargePointRequest.builder()
                .amount(amount)
                .accountNumber(account.getAccountNumber())
                .build();

        mvc.perform(
                        post("/accounts/points")
                                .header("Auth", token)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorFields").exists())
                .andDo(print());
    }

    @DisplayName("통합테스트 포인트 충전 실패 - 200만원 보다 큰 경우")
    @Test
    void charge_point_fail_greater_than_2000000() throws Exception{
        Member owner = MemberFactory.member();
        memberService.signUp(owner);
        Account account = accountService.createAccount(owner);
        int amount = 20000000;
        String token = tokenService.generateToken(owner.getEmail());

        AccountDTO.ChargePointRequest request = AccountDTO.ChargePointRequest.builder()
                .amount(amount)
                .accountNumber(account.getAccountNumber())
                .build();

        mvc.perform(
                        post("/accounts/points")
                                .header("Auth", token)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.errorFields").exists())
                .andDo(print());
    }

    @DisplayName("통합테스트 포인트 충전 실패 - 계좌 소유주가 아닐 경우")
    @Test
    void charge_point_fail_requester_not_owner() throws Exception{
        Member owner = MemberFactory.member();
        Member requester = Member.builder()
                .email("requester")
                .name("requester")
                .password("1231242314123")
                .build();

        memberService.signUp(owner);
        memberService.signUp(requester);

        Account account = accountService.createAccount(owner);

        int amount = 50000;
        String token = tokenService.generateToken(requester.getEmail());

        AccountDTO.ChargePointRequest request = AccountDTO.ChargePointRequest.builder()
                .amount(amount)
                .accountNumber(account.getAccountNumber())
                .build();

        mvc.perform(
                        post("/accounts/points")
                                .header("Auth", token)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.REQUESTER_IS_NOT_OWNER.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.REQUESTER_IS_NOT_OWNER.getDescription()))
                .andDo(print());
    }

    @DisplayName("통합테스트 포인트 충전 실패 - 로그인 하지 않은 유저")
    @Test
    void charge_point_fail_without_token() throws Exception{
        Member owner = MemberFactory.member();
        memberService.signUp(owner);
        Account account = accountService.createAccount(owner);
        int amount = 50000;

        AccountDTO.ChargePointRequest request = AccountDTO.ChargePointRequest.builder()
                .amount(amount)
                .accountNumber(account.getAccountNumber())
                .build();

        mvc.perform(
                        post("/accounts/points")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.UN_AUTHORIZED.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UN_AUTHORIZED.getDescription()))
                .andDo(print());
    }

    @DisplayName("통합테스트 포인트 충전 실패 - 계좌번호 비어 있는 경우")
    @Test
    void charge_point_fail_account_number_is_empty() throws Exception{
        Member owner = MemberFactory.member();
        memberService.signUp(owner);
        int amount = 50000;

        AccountDTO.ChargePointRequest request = AccountDTO.ChargePointRequest.builder()
                .amount(amount)
                .build();

        mvc.perform(
                        post("/accounts/points")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.BAD_REQUEST.toString()))
                .andExpect(jsonPath("$.errorFields").exists())
                .andDo(print());
    }

    @DisplayName("통합테스트 포인트 충전 실패 - 계좌번호 12자리 아닌 경우")
    @Test
    void charge_point_fail_account_number_length_is_not_12() throws Exception{
        Member owner = MemberFactory.member();
        memberService.signUp(owner);
        int amount = 50000;

        AccountDTO.ChargePointRequest request = AccountDTO.ChargePointRequest.builder()
                .amount(amount)
                .accountNumber("123")
                .build();

        mvc.perform(
                        post("/accounts/points")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.BAD_REQUEST.toString()))
                .andExpect(jsonPath("$.errorFields").exists())
                .andDo(print());
    }

    @DisplayName("통합테스트 포인트 충전 실패 - 계좌번호 숫자 아닌 문자 포함")
    @Test
    void charge_point_fail_account_number_contains_not_numeric_character() throws Exception{
        Member owner = MemberFactory.member();
        memberService.signUp(owner);
        int amount = 50000;

        AccountDTO.ChargePointRequest request = AccountDTO.ChargePointRequest.builder()
                .amount(amount)
                .accountNumber("123456789rrr")
                .build();

        mvc.perform(
                        post("/accounts/points")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.BAD_REQUEST.toString()))
                .andExpect(jsonPath("$.errorFields").exists())
                .andDo(print());
    }

    @DisplayName("통합테스트 포인트 충전 실패 - 존재하지 않는 계좌")
    @Test
    void charge_point_fail_account_not_found() throws Exception{
        Member owner = MemberFactory.member();
        memberService.signUp(owner);
        int amount = 50000;

        String token = tokenService.generateToken(owner.getEmail());

        AccountDTO.ChargePointRequest request = AccountDTO.ChargePointRequest.builder()
                .amount(amount)
                .accountNumber("123456789111")
                .build();

        mvc.perform(
                        post("/accounts/points")
                                .header("Auth", token)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.ACCOUNT_NOT_FOUND.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.ACCOUNT_NOT_FOUND.getDescription()))
                .andDo(print());
    }
}
