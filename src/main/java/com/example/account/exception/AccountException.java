package com.example.account.exception;

import com.example.account.type.ErrorCode;
import lombok.*;

// 그냥 Exception을 상속받으면 checked exception이기 때문에
// 항상 메서드의 시그니처에 exception을 줄줄이 붙여야하는 불편함도있고,
// 기본적으로 checked exception은 트랜잭션을 rollback해주는 대상에서 제외됨.
// -> 일반적으로 RuntimeException 기반해 사용.

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountException extends RuntimeException {
    private ErrorCode errorCode;
    private String errorMessage;

    // AccountException은 AllArgsConstructor, NoArgsConstructor 두개의 생성자를 가지기 때문에
    // new AccountException(ErrorCode.USER_NOT_FOUND)로 객체 생성 시 생성자가 없어서 에러발생.
    // 따라서 하나의 인자를 가지는 생성자 추가생성.
    public AccountException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getDescription();
    }
}
