package com.jungmini.pay.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SHA256EncoderTest {

    private SHA256Encoder sha256Encoder = new SHA256Encoder();

    @Test
    @DisplayName("SHA-256 암호화 테스트")
    void encode() {
        String plain = "test";
        String encodedString = sha256Encoder.encode(plain);

        assertThat(encodedString.length()).isEqualTo(64);
    }

    @Test
    @DisplayName("SHA-256 암호화 검증 테스트 맞는 비밀번호")
    void verify_true() {
        String plain = "test";
        String encodedString = sha256Encoder.encode(plain);

        boolean result = sha256Encoder.verify("test", encodedString);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("SHA-256 암호화 검증 테스트 틀린 비밀번호")
    void verify_false() {
        String plain = "testxxxxx";
        String encodedString = sha256Encoder.encode(plain);

        boolean result = sha256Encoder.verify("test", encodedString);
        assertThat(result).isFalse();
    }
}