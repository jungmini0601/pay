package com.jungmini.pay.service;

import com.jungmini.pay.domain.Member;
import com.jungmini.pay.common.exception.ErrorCode;
import com.jungmini.pay.common.exception.PayException;
import com.jungmini.pay.fixture.MemberFactory;
import com.jungmini.pay.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원가입 성공")
    void signUp_success() {
        Member member = MemberFactory.member();
        given(passwordEncoder.encode(any())).willReturn("encodedPassword");
        given(memberRepository.findById(any())).willReturn(Optional.empty());
        given(memberRepository.save(any())).willReturn(
                Member.builder()
                        .email(member.getEmail())
                        .password("encodedPassword")
                        .name(member.getName())
                        .build());

        Member signUpMember = memberService.signUp(member);

        assertThat(signUpMember.getEmail()).isEqualTo(member.getEmail());
        assertThat(signUpMember.getPassword()).isEqualTo("encodedPassword");
        assertThat(signUpMember.getName()).isEqualTo(member.getName());
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 회원")
    void signUp_fail_duplicated_member() {
        Member member = MemberFactory.member();
        given(memberRepository.findById(any())).willReturn(Optional.of(member));

        PayException payException = assertThrows(PayException.class, () -> {
            memberService.signUp(member);
        });

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.MEMBER_DUPLICATED.toString());
        assertThat(payException.getErrorMessage()).isEqualTo(ErrorCode.MEMBER_DUPLICATED.getDescription());
    }

    @Test
    @DisplayName("로그인 성공")
    void singin_success() {
        Member member = MemberFactory.member();
        given(memberRepository.findById(any())).willReturn(Optional.of(member));
        given(passwordEncoder.verify(any(), any())).willReturn(true);
        given(tokenService.generateToken(any())).willReturn("token");

        String token = memberService.signin(member);
        assertThat(token).isEqualTo("token");
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 유저")
    void singin_fail_member_not_found() {
        Member member = MemberFactory.member();
        given(memberRepository.findById(any())).willReturn(Optional.empty());

        PayException exception = assertThrows(PayException.class, () -> {
            memberService.signin(member);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.toString());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 일치 X")
    void singin_fail_password_mismatch() {
        Member member = MemberFactory.member();
        given(memberRepository.findById(any())).willReturn(Optional.of(member));
        given(passwordEncoder.verify(any(), any())).willReturn(false);

        PayException exception = assertThrows(PayException.class, () -> {
            memberService.signin(member);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_MISMATCH.toString());
    }
}