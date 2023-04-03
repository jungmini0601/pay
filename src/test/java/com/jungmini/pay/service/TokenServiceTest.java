package com.jungmini.pay.service;

import com.jungmini.pay.exception.ErrorCode;
import com.jungmini.pay.exception.PayException;
import com.jungmini.pay.fixture.MemberFactory;
import com.jungmini.pay.properties.JwtProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceTest {

    private JwtProperties jwtProperties = new JwtProperties("asdjkfhasdjlfhasdjklfhklsdjfhlaskdjfahlsdjfh", 10000000);
    private TokenService tokenService =
            new TokenService(jwtProperties);

    @Test
    @DisplayName("토큰 생성 테스트 성공")
    void generate_token() {
        String email = MemberFactory.member().getEmail();
        String token = tokenService.generateToken(email);

        assertThat(token).isNotBlank();
        assertThat(token).isNotNull();
        System.out.println(token);
    }

    @Test
    @DisplayName("토큰 검증 테스트 성공")
    void verify_token_success() {
        String email = MemberFactory.member().getEmail();
        String token = tokenService.generateToken(email);

        String parsedEmail = tokenService.verifyToken(token);
        assertThat(email).isEqualTo(parsedEmail);
    }

    @Test
    @DisplayName("토큰 검증 실패 테스트 - 토큰 변조된 경우")
    void verify_token_fail() {
        String email = MemberFactory.member().getEmail();
        String token = tokenService.generateToken(email);

        PayException exception = Assertions.assertThrows(PayException.class, () -> {
            tokenService.verifyToken(token + "abcd");
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TOKEN_INVALID.toString());
    }

    @Test
    @DisplayName("토큰 검증 실패 테스트 - 토큰 유효기간 지난 경우")
    void verify_token_fail_timeout() throws Exception {
        jwtProperties = new JwtProperties("asdjkfhasdjlfhasdjklfhklsdjfhlaskdjfahlsdjfh", 1);
        tokenService = new TokenService(jwtProperties);

        String email = MemberFactory.member().getEmail();
        String token = tokenService.generateToken(email);

        Thread.sleep(1000L);

        PayException exception = Assertions.assertThrows(PayException.class, () -> {
            tokenService.verifyToken(token);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TOKEN_EXPIRED.toString());
    }
}