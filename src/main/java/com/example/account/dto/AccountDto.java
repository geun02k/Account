package com.example.account.dto;

import com.example.account.domain.Account;
import lombok.*;

import java.time.LocalDateTime;

// DTO
// : controller와 service 사이 데이터 전달 시 사용.
//   Entity 클래스가 있으면 Entity 클래스와 거의 비슷하지만 단순하게 작성.(필요한 데이터만 작성)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {
    private Long userId;
    private String accountNumber;
    private Long balance;

    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;

    // 특정Entity타입 -> 특정DTO타입으로 변경할 때 자주 사용하는 방법.
    public static AccountDto fromEntity(Account account) {
    return AccountDto.builder()
            .userId(account.getAccountUser().getId())
            .accountNumber(account.getAccountNumber())
            .balance(account.getBalance())
            .registeredAt(account.getRegisteredAt())
            .unRegisteredAt(account.getUnRegisteredAt())
            .build();
    }
}
