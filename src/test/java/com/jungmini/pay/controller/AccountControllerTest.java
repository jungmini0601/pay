package com.jungmini.pay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jungmini.pay.common.exception.ErrorCode;
import com.jungmini.pay.controller.dto.AccountDTO;
import com.jungmini.pay.domain.Account;
import com.jungmini.pay.domain.FriendRequest;
import com.jungmini.pay.domain.Member;
import com.jungmini.pay.domain.Transaction;
import com.jungmini.pay.domain.type.AccountStatus;
import com.jungmini.pay.fixture.*;
import com.jungmini.pay.service.AccountService;
import com.jungmini.pay.service.FriendService;
import com.jungmini.pay.service.MemberService;
import com.jungmini.pay.service.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Autowired
    private FriendService friendService;

    @DisplayName("통합테스트 계좌 생성 성공 - 첫 번째 계좌 생성")
    @Test
    void create_first_account_success() throws Exception {
        Member member = MemberFactory.member();
        // 회원가입
        memberService.signUp(member);
        // 로그인
        String token = tokenService.generateToken(member.getEmail());
        // 계좌 생성
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
        // 회원가입
        memberService.signUp(member);
        // 계좌 생성
        accountService.createAccount(member);
        // 로그인
        String token = tokenService.generateToken(member.getEmail());
        // 계좌 생성 요청
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
        // 회원가입
        memberService.signUp(member);
        // 계좌를 최대 사이즈 만큼 생성
        for (int i = 0; i < Account.MAX_ACCOUNT_SIZE; i++) {
            accountService.createAccount(member);
        }
        // 로그인
        String token = tokenService.generateToken(member.getEmail());
        // 추가 계좌 개설 요청
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
        int amount = 10000;
        // 회원가입
        memberService.signUp(owner);
        // 계좌 생성
        Account account = accountService.createAccount(owner);
        // 로그인
        String token = tokenService.generateToken(owner.getEmail());
        // 포인트 충전 요청
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
        int amount = 2000000;
        // 회원가입
        memberService.signUp(owner);
        // 계좌 생성
        Account account = accountService.createAccount(owner);
        // 로그인
        String token = tokenService.generateToken(owner.getEmail());
        // 포인트 충전 요청
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
        int amount = 999;
        // 회원가입
        memberService.signUp(owner);
        // 계좌 생성
        Account account = accountService.createAccount(owner);
        // 토큰 생성
        String token = tokenService.generateToken(owner.getEmail());
        // 포인트 충전 요청
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
        int amount = 20000000;
        // 회원가입
        memberService.signUp(owner);
        // 계좌 생성
        Account account = accountService.createAccount(owner);
        // 로그인
        String token = tokenService.generateToken(owner.getEmail());
        // 포인트 충전 요청
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
        int amount = 50000;
        // 회원가입
        memberService.signUp(owner);
        memberService.signUp(requester);
        // 계좌 생성
        Account account = accountService.createAccount(owner);
        // 요청자 로그인
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
        int amount = 50000;
        // 회원 가입
        memberService.signUp(owner);
        // 계좌 생성
        Account account = accountService.createAccount(owner);
        // 충전 요청
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
        int amount = 50000;
        // 회원 가입
        memberService.signUp(owner);

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
        int amount = 50000;
        // 회원 가입
        memberService.signUp(owner);
        // 충전 요청
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
        int amount = 50000;
        // 회원 가입
        memberService.signUp(owner);
        // 충전 요청
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
        int amount = 50000;
        // 회원 가입
        memberService.signUp(owner);
        // 로그인
        String token = tokenService.generateToken(owner.getEmail());
        // 충전 요청
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

    @DisplayName("통합테스트 송금 성공")
    @Test
    void remit_success() throws Exception{
        int remitterBalance = 10000;
        int recipientBalance = 100;
        int amount = 500;
        Member remitter = MemberFactory.memberFrom("remitter@test.com", "123465789");
        Member recipient = MemberFactory.memberFrom("recipient@test.com", "123456789");

        // 회원가입
        memberService.signUp(remitter);
        memberService.signUp(recipient);
        // 로그인
        String remitterToken = tokenService.generateToken(remitter.getEmail());
        // 계좌생성
        Account remitterAccount = accountService.createAccount(remitter);
        Account recipientAccount = accountService.createAccount(recipient);
        // 잔액 충전
        accountService.chargePoint(remitterBalance, remitterAccount, remitter);
        accountService.chargePoint(recipientBalance, recipientAccount, recipient);
        // 친구관계 생성
        FriendRequest friendRequest = FriendRequest.from(remitter, recipient);
        FriendRequest savedRequest = friendService.requestFriend(friendRequest);
        friendService.acceptFriendRequest(savedRequest.getId());
        // 송금
        AccountDTO.RemitRequest remitRequest = AccountDTO.RemitRequest.builder()
                .amount(amount)
                .recipientsAccountNumber(recipientAccount.getAccountNumber())
                .remitterAccountNumber(remitterAccount.getAccountNumber())
                .build();

        mvc.perform(
                post("/accounts/remit")
                    .header("Auth", remitterToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(remitRequest)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.amount").value(amount))
                .andExpect(jsonPath("$.recipientsAccountNumber").value(recipientAccount.getAccountNumber()))
                .andExpect(jsonPath("$.remitterAccountNumber").value(remitterAccount.getAccountNumber()))
                .andExpect(jsonPath("createdAt").exists())
                .andDo(print());
    }

    @DisplayName("통합테스트 송금 실패 - 잔액 부족")
    @Test
    void remit_fail_lack_of_balance() throws Exception{
        int remitterBalance = 10000;
        int recipientBalance = 100;
        int amount = 500000;
        Member remitter = MemberFactory.memberFrom("remitter@test.com", "123465789");
        Member recipient = MemberFactory.memberFrom("recipient@test.com", "123456789");

        // 회원가입
        memberService.signUp(remitter);
        memberService.signUp(recipient);
        // 로그인
        String remitterToken = tokenService.generateToken(remitter.getEmail());
        // 계좌생성
        Account remitterAccount = accountService.createAccount(remitter);
        Account recipientAccount = accountService.createAccount(recipient);
        // 잔액 충전
        accountService.chargePoint(remitterBalance, remitterAccount, remitter);
        accountService.chargePoint(recipientBalance, recipientAccount, recipient);
        // 친구관계 생성
        FriendRequest friendRequest = FriendRequest.from(remitter, recipient);
        FriendRequest savedRequest = friendService.requestFriend(friendRequest);
        friendService.acceptFriendRequest(savedRequest.getId());
        // 송금
        AccountDTO.RemitRequest remitRequest = AccountDTO.RemitRequest.builder()
                .amount(amount)
                .recipientsAccountNumber(recipientAccount.getAccountNumber())
                .remitterAccountNumber(remitterAccount.getAccountNumber())
                .build();

        mvc.perform(
                post("/accounts/remit")
                    .header("Auth", remitterToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(remitRequest)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.LACK_OF_BALANCE.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.LACK_OF_BALANCE.getDescription()))
                .andDo(print());
    }

    @DisplayName("통합테스트 송금 실패 - 친구 아닌 계좌에 송금")
    @Test
    void remit_fail_not_friend() throws Exception{
        int remitterBalance = 10000;
        int recipientBalance = 100;
        int amount = 500;
        Member remitter = MemberFactory.memberFrom("remitter@test.com", "123465789");
        Member recipient = MemberFactory.memberFrom("recipient@test.com", "123456789");

        // 회원가입
        memberService.signUp(remitter);
        memberService.signUp(recipient);
        // 로그인
        String remitterToken = tokenService.generateToken(remitter.getEmail());
        // 계좌생성
        Account remitterAccount = accountService.createAccount(remitter);
        Account recipientAccount = accountService.createAccount(recipient);
        // 잔액 충전
        accountService.chargePoint(remitterBalance, remitterAccount, remitter);
        accountService.chargePoint(recipientBalance, recipientAccount, recipient);
        // 송금
        AccountDTO.RemitRequest remitRequest = AccountDTO.RemitRequest.builder()
                .amount(amount)
                .recipientsAccountNumber(recipientAccount.getAccountNumber())
                .remitterAccountNumber(remitterAccount.getAccountNumber())
                .build();

        mvc.perform(
                post("/accounts/remit")
                    .header("Auth", remitterToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(remitRequest)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_FRIENDS.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.NOT_FRIENDS.getDescription()))
                .andDo(print());
    }

    @DisplayName("통합테스트 송금 실패 - 토큰 X")
    @Test
    void remit_fail_without_token() throws Exception{
        int remitterBalance = 10000;
        int recipientBalance = 100;
        int amount = 500;
        Member remitter = MemberFactory.memberFrom("remitter@test.com", "123465789");
        Member recipient = MemberFactory.memberFrom("recipient@test.com", "123456789");

        // 회원가입
        memberService.signUp(remitter);
        memberService.signUp(recipient);
        // 계좌생성
        Account remitterAccount = accountService.createAccount(remitter);
        Account recipientAccount = accountService.createAccount(recipient);
        // 잔액 충전
        accountService.chargePoint(remitterBalance, remitterAccount, remitter);
        accountService.chargePoint(recipientBalance, recipientAccount, recipient);
        // 송금
        AccountDTO.RemitRequest remitRequest = AccountDTO.RemitRequest.builder()
                .amount(amount)
                .recipientsAccountNumber(recipientAccount.getAccountNumber())
                .remitterAccountNumber(remitterAccount.getAccountNumber())
                .build();

        mvc.perform(
                post("/accounts/remit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(remitRequest)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.UN_AUTHORIZED.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UN_AUTHORIZED.getDescription()))
                .andDo(print());
    }

    @DisplayName("통합테스트 송금 실패 - 계좌 소유주가 아닌 경우")
    @Test
    void remit_fail_not_owner() throws Exception{
        int remitterBalance = 10000;
        int recipientBalance = 100;
        int amount = 500;
        Member remitter = MemberFactory.memberFrom("remitter@test.com", "123465789");
        Member recipient = MemberFactory.memberFrom("recipient@test.com", "123456789");
        Member member = MemberFactory.memberFrom("test@test.com", "123456789");
        // 회원가입
        memberService.signUp(remitter);
        memberService.signUp(recipient);
        memberService.signUp(member);
        // 로그인
        String memberToken = tokenService.generateToken(member.getEmail());
        // 계좌생성
        Account remitterAccount = accountService.createAccount(remitter);
        Account recipientAccount = accountService.createAccount(recipient);
        // 잔액 충전
        accountService.chargePoint(remitterBalance, remitterAccount, remitter);
        accountService.chargePoint(recipientBalance, recipientAccount, recipient);
        // 송금
        AccountDTO.RemitRequest remitRequest = AccountDTO.RemitRequest.builder()
                .amount(amount)
                .recipientsAccountNumber(recipientAccount.getAccountNumber())
                .remitterAccountNumber(remitterAccount.getAccountNumber())
                .build();

        mvc.perform(
                post("/accounts/remit")
                    .header("Auth", memberToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(remitRequest)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.REQUESTER_IS_NOT_OWNER.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.REQUESTER_IS_NOT_OWNER.getDescription()))
                .andDo(print());
    }

    @DisplayName("통합테스트 송금 실패 - 존재 하지 않는 계좌인 경우")
    @Test
    void remit_fail_account_not_found() throws Exception{
        int amount = 500;
        Member remitter = MemberFactory.memberFrom("remitter@test.com", "123465789");
        Member recipient = MemberFactory.memberFrom("recipient@test.com", "123456789");
        // 회원가입
        memberService.signUp(remitter);
        memberService.signUp(recipient);
        // 로그인
        String remitterToken = tokenService.generateToken(remitter.getEmail());
        // 송금
        AccountDTO.RemitRequest remitRequest = AccountDTO.RemitRequest.builder()
                .amount(amount)
                .recipientsAccountNumber("100000000001")
                .remitterAccountNumber("100000000000")
                .build();

        mvc.perform(
                post("/accounts/remit")
                    .header("Auth", remitterToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(remitRequest)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.ACCOUNT_NOT_FOUND.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.ACCOUNT_NOT_FOUND.getDescription()))
                .andDo(print());
    }

    @DisplayName("통합테스트 계좌 상세 조회 성공")
    @Test
    void get_account_info_success() throws Exception {
        Member member = MemberFactory.memberFrom("test@test.com", "123456789");
        //회원가입
        memberService.signUp(member);
        //로그인
        String token = tokenService.generateToken(member.getEmail());
        //계좌생성
        Account account = accountService.createAccount(member);
        //계좌조회
        mvc.perform(
                get("/accounts/" + account.getAccountNumber())
                    .header("Auth", token))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.balance").value(account.getBalance()))
                .andExpect(jsonPath("$.ownerEmail").value(account.getOwner().getEmail()))
                .andExpect(jsonPath("$.accountNumber").value(account.getAccountNumber()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andDo(print());
    }

    @DisplayName("통합테스트 계좌 상세 조회 실패 - 토큰 X")
    @Test
    void get_account_info_fail_without_token() throws Exception {
        Member member = MemberFactory.memberFrom("test@test.com", "123456789");
        //회원가입
        memberService.signUp(member);
        //계좌생성
        Account account = accountService.createAccount(member);
        //계좌조회
        mvc.perform(
                get("/accounts/" + account.getAccountNumber()))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.UN_AUTHORIZED.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UN_AUTHORIZED.getDescription()))
                .andDo(print());
    }

    @DisplayName("통합테스트 계좌 상세 조회 실패 - 계좌 소유주 아님")
    @Test
    void get_account_info_fail_not_owner() throws Exception {
        Member member = MemberFactory.memberFrom("test@test.com", "123456789");
        Member member2 = MemberFactory.memberFrom("test2@test.com", "123456789");
        //회원가입
        memberService.signUp(member);
        memberService.signUp(member2);
        //로그인
        String token = tokenService.generateToken(member.getEmail());
        //계좌생성
        Account account2 = accountService.createAccount(member2);
        //계좌조회
        mvc.perform(
                        get("/accounts/" + account2.getAccountNumber())
                                .header("Auth", token))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.REQUESTER_IS_NOT_OWNER.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.REQUESTER_IS_NOT_OWNER.getDescription()))
                .andDo(print());
    }

    @DisplayName("통합테스트 계좌 상세 조회 실패 - 계좌 번호 검증 실패")
    @Test
    void get_account_info_fail_invalid_account_number() throws Exception {
        Member member = MemberFactory.memberFrom("test@test.com", "123456789");
        String invalidAccountNumber = "105";
        //회원가입
        memberService.signUp(member);
        //로그인
        String token = tokenService.generateToken(member.getEmail());
        //계좌조회
        mvc.perform(
                        get("/accounts/" + invalidAccountNumber)
                                .header("Auth", token))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.ILLEGAL_ACCOUNT_NUMBER.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.ILLEGAL_ACCOUNT_NUMBER.getDescription()))
                .andDo(print());
    }

    @DisplayName("통합테스트 거래 내역 조회 성공")
    @Test
    void get_transactions_success() throws Exception {
        int remitterBalance = 10000;
        int recipientBalance = 100;
        int amount = 500;
        Member remitter = MemberFactory.memberFrom("remitter@test.com", "123465789");
        Member recipient = MemberFactory.memberFrom("recipient@test.com", "123456789");

        // 회원가입
        memberService.signUp(remitter);
        memberService.signUp(recipient);
        // 로그인
        String remitterToken = tokenService.generateToken(remitter.getEmail());
        // 계좌생성
        Account remitterAccount = accountService.createAccount(remitter);
        Account recipientAccount = accountService.createAccount(recipient);
        // 잔액 충전
        accountService.chargePoint(remitterBalance, remitterAccount, remitter);
        accountService.chargePoint(recipientBalance, recipientAccount, recipient);
        // 친구관계 생성
        FriendRequest friendRequest = FriendRequest.from(remitter, recipient);
        FriendRequest savedRequest = friendService.requestFriend(friendRequest);
        friendService.acceptFriendRequest(savedRequest.getId());
        // 송금
        Transaction request1 = TransactionFactory.transactionRequest(remitterAccount, recipientAccount, amount);
        Transaction request2 = TransactionFactory.transactionRequest(remitterAccount, recipientAccount, amount);
        accountService.remit(request1, remitter);
        accountService.remit(request2, remitter);
        // 거래 내역 조회
        mvc.perform(
                get(String.format("/accounts/%s/transactions?page=0&size=20", remitterAccount.getAccountNumber()))
                    .header("Auth", remitterToken))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.[0]").exists())
                .andExpect(jsonPath("$.[1]").exists())
                .andDo(print());
    }

    @DisplayName("통합테스트 거래 내역 조회 실패 - 내 계좌가 아닌 경우")
    @Test
    void get_transactions_fail_requeseter_not_onwer() throws Exception {
        int remitterBalance = 10000;
        int recipientBalance = 100;
        int amount = 500;
        Member remitter = MemberFactory.memberFrom("remitter@test.com", "123465789");
        Member recipient = MemberFactory.memberFrom("recipient@test.com", "123456789");

        // 회원가입
        memberService.signUp(remitter);
        memberService.signUp(recipient);
        // 로그인
        String recipientToken = tokenService.generateToken(recipient.getEmail());
        // 계좌생성
        Account remitterAccount = accountService.createAccount(remitter);
        Account recipientAccount = accountService.createAccount(recipient);
        // 잔액 충전
        accountService.chargePoint(remitterBalance, remitterAccount, remitter);
        accountService.chargePoint(recipientBalance, recipientAccount, recipient);
        // 친구관계 생성
        FriendRequest friendRequest = FriendRequest.from(remitter, recipient);
        FriendRequest savedRequest = friendService.requestFriend(friendRequest);
        friendService.acceptFriendRequest(savedRequest.getId());
        // 송금
        Transaction request1 = TransactionFactory.transactionRequest(remitterAccount, recipientAccount, amount);
        Transaction request2 = TransactionFactory.transactionRequest(remitterAccount, recipientAccount, amount);
        accountService.remit(request1, remitter);
        accountService.remit(request2, remitter);
        // 거래 내역 조회
        mvc.perform(
                    get(String.format("/accounts/%s/transactions?page=0&size=20", remitterAccount.getAccountNumber()))
                        .header("Auth", recipientToken))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.REQUESTER_IS_NOT_OWNER.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.REQUESTER_IS_NOT_OWNER.getDescription()))
                .andDo(print());
    }

    @DisplayName("통합테스트 거래 내역 조회 실패 - 계좌 못 찾은 경우")
    @Test
    void get_transactions_fail_account_not_found() throws Exception {
        int remitterBalance = 10000;
        int recipientBalance = 100;
        int amount = 500;
        Member remitter = MemberFactory.memberFrom("remitter@test.com", "123465789");
        Member recipient = MemberFactory.memberFrom("recipient@test.com", "123456789");

        // 회원가입
        memberService.signUp(remitter);
        memberService.signUp(recipient);
        // 로그인
        String recipientToken = tokenService.generateToken(recipient.getEmail());
        // 계좌생성
        Account remitterAccount = accountService.createAccount(remitter);
        Account recipientAccount = accountService.createAccount(recipient);
        // 잔액 충전
        accountService.chargePoint(remitterBalance, remitterAccount, remitter);
        accountService.chargePoint(recipientBalance, recipientAccount, recipient);
        // 친구관계 생성
        FriendRequest friendRequest = FriendRequest.from(remitter, recipient);
        FriendRequest savedRequest = friendService.requestFriend(friendRequest);
        friendService.acceptFriendRequest(savedRequest.getId());
        // 송금
        Transaction request1 = TransactionFactory.transactionRequest(remitterAccount, recipientAccount, amount);
        Transaction request2 = TransactionFactory.transactionRequest(remitterAccount, recipientAccount, amount);
        accountService.remit(request1, remitter);
        accountService.remit(request2, remitter);
        // 거래 내역 조회
        mvc.perform(
                get(String.format("/accounts/%s/transactions?page=0&size=20", "100008888888"))
                    .header("Auth", recipientToken))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.ACCOUNT_NOT_FOUND.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.ACCOUNT_NOT_FOUND.getDescription()))
                .andDo(print());
    }

    @DisplayName("통합테스트 거래 내역 조회 실패 - 유효 하지 않은 계좌 번호")
    @Test
    void get_transactions_fail_illegal_account_number() throws Exception {
        int remitterBalance = 10000;
        int recipientBalance = 100;
        int amount = 500;
        Member remitter = MemberFactory.memberFrom("remitter@test.com", "123465789");
        Member recipient = MemberFactory.memberFrom("recipient@test.com", "123456789");

        // 회원가입
        memberService.signUp(remitter);
        memberService.signUp(recipient);
        // 로그인
        String recipientToken = tokenService.generateToken(recipient.getEmail());
        // 계좌생성
        Account remitterAccount = accountService.createAccount(remitter);
        Account recipientAccount = accountService.createAccount(recipient);
        // 잔액 충전
        accountService.chargePoint(remitterBalance, remitterAccount, remitter);
        accountService.chargePoint(recipientBalance, recipientAccount, recipient);
        // 친구관계 생성
        FriendRequest friendRequest = FriendRequest.from(remitter, recipient);
        FriendRequest savedRequest = friendService.requestFriend(friendRequest);
        friendService.acceptFriendRequest(savedRequest.getId());
        // 송금
        Transaction request1 = TransactionFactory.transactionRequest(remitterAccount, recipientAccount, amount);
        Transaction request2 = TransactionFactory.transactionRequest(remitterAccount, recipientAccount, amount);
        accountService.remit(request1, remitter);
        accountService.remit(request2, remitter);
        // 거래 내역 조회
        mvc.perform(
                get(String.format("/accounts/%s/transactions?page=0&size=20", "10000888888z"))
                    .header("Auth", recipientToken))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.ILLEGAL_ACCOUNT_NUMBER.toString()))
                .andExpect(jsonPath("$.message").value(ErrorCode.ILLEGAL_ACCOUNT_NUMBER.getDescription()))
                .andDo(print());
    }
}
