package com.example.account.dto;

import lombok.*;

// 클라이언트와 controller 사이에 데이터 전달 시 사용.
// 비슷한 내용의 DTO를 계속 생성한다고 느낄 수 있지만
// 전용 DTO가 아닌 다목적 DTO를 사용하게 되면 추후 복잡한 상황에서
// 의도치않은 장애가 발생할 수 있기에 따로 둔다.

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountInfo {
    private String accountNumber;
    private Long balance;
}
