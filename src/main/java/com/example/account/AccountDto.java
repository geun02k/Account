package com.example.account;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class AccountDto {
    private String accountNumber; // 계좌번호
    private String nickname; // 계좌명
    private LocalDateTime registeredAt; // 계좌등록일시
}
