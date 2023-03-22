package com.jungmini.pay.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    BAD_REQUEST("잘못된 요청 값입니다."),
    MEMBER_DUPLICATED("중복된 회원 입니다."),
    ;
    private final String description;

}
