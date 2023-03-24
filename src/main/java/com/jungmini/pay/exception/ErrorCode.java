package com.jungmini.pay.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    BAD_REQUEST("잘못된 요청 값입니다."),
    MEMBER_DUPLICATED("중복된 회원 입니다."),
    PASSWORD_MISMATCH("이메일 비밀번호를 확인 해주세요"),
    TOKEN_INVALID("토큰이 유효하지 않습니다."),
    TOKEN_EXPIRED("토큰 유효기간이 만료되었습니다.");
    private final String description;

}
