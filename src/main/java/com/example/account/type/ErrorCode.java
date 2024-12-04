package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

// @AllArgsConstructor 사용
// -> description 들어가는 생성자를 자동생성.
// -> 코드에 description 작성가능.

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 에러코드는 축약하기 보다는 풀네임을 사용하는 추세.
    USER_NOT_FOUND("사용자가 없습니다."),
    ACCOUNT_NOT_FOUND("계좌가 없습니다."),
    AMOUNT_EXCEED_BALANCE("거래 금액이 계좌 잔액보다 큽니다."),
    USER_ACCOUNT_UN_MATCH("사용자와 계좌의 소유주가 다릅니다."),
    ACCOUNT_ALREADY_UNREGISTERED("계좌가 이미 해지되었습니다."),
    BALANCE_NOT_EMPTY("잔액이 있는 계좌는 해지할 수 없습니다."),
    MAX_ACCOUNT_PER_USER_10("사용자 최대 계좌는 10개 입니다.")
    ;

    // description을 이용해 코드에 대한 정보를 입력해주면 추후 이해하기 편한 이점있음.
    // (영어코드만 사용하다보면 모호한 경우가 가끔 존재.)
    private final String description;
}
