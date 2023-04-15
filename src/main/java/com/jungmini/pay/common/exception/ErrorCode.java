package com.jungmini.pay.common.exception;

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
    ILLEGAL_ACCOUNT_NUMBER("계좌 번호는 12자리 숫자여야 합니다."),
    ACCOUNT_SIZE_EXCEED("최대 계좌 개설 수를 초과 했습니다."),
    ACCOUNT_NOT_FOUND("존재 하지 않는 계좌 입니다."),
    ACCOUNT_LOCK_FAIL("계좌 잠금 획득 실패"),
    REQUESTER_IS_NOT_OWNER("계좌 소유주가 아닙니다."),
    ILLEGAL_TRANSACTION_STATE("유효하지 않은 거래 상태입니다."),
    LACK_OF_BALANCE("잔액이 부족 합니다"),
    NOT_FRIENDS("친구 관계만 송금이 가능합니다"),
    UN_AUTHORIZED("로그인이 필요 합니다.");
    private final String description;

}
