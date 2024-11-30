package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

// @AllArgsConstructor 사용
// -> description 들어가는 생성자를 자동생성.
// -> 코드에 description 작성가능.

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("사용자가 없습니다.");

    // description을 이용해 코드에 대한 정보를 입력해주면 추후 이해하기 편한 이점있음.
    // (영어코드만 사용하다보면 모호한 경우가 가끔 존재.)
    private final String description;
}
