package com.jungmini.pay.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    BAD_REQUEST("잘못된 요청 값입니다."),
    MEMBER_DUPLICATED("중복된 회원 입니다."),
    MEMBER_NOT_FOUND("회원을 찾을 수 없습니다."),
    PASSWORD_MISMATCH("이메일 비밀번호를 확인 해주세요"),
    TOKEN_INVALID("토큰이 유효하지 않습니다."),
    TOKEN_EXPIRED("토큰 유효기간이 만료되었습니다."),
    ALREADY_FRIENDS("이미 친구 관계입니다."),
    FRIENDS_REQUEST_EXISTS("이미 친구 요청이 존재합니다."),
    SELF_FRIEND_REQUEST("스스로에게 친구 요청을 할 수 없습니다."),
    SIGN_IN_REQUIRED("로그인이 필요 합니다.");
    private final String description;

}
