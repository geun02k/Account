package com.example.account.dto;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

// CreateAccount의 이너클래스로 Request, Response 모두 작성
public class CreateAccount {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Request {
        // 필드에 Validation 추가
        @NotNull
        @Min(1)
        private Long userId;

        @NotNull
        @Min(0) // http 삭제요청 테스트를 위해 임의변경(계좌잔액이 0원 이상이면 해지불가)
        private Long initialBalance;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long userId;
        private String accountNumber;
        private LocalDateTime registeredAt;

        public static Response from(AccountDto accountDto) {
            return Response.builder()
                    .userId(accountDto.getUserId())
                    .accountNumber(accountDto.getAccountNumber())
                    .registeredAt(accountDto.getRegisteredAt())
                    .build();
        }
    }
}
