package com.example.account.domain;

import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

// 테이블과 1:1 매칭되는 Entity 객체
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Transaction {
    @Id // Transaction테이블의 PK를 id로 지정.
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING) // 입력한 enum 문자열이름을 그대로 DB에 저장.
    private TransactionType transactionType; // 사용, 사용취소
    @Enumerated(EnumType.STRING)
    private TransactionResultType transactionResultType; // 성공, 실패

    @ManyToOne // 거래내역-계좌는 n:1의 관계
    private Account account; // 계좌정보
    private Long amount; // 거래금액
    private Long balanceSnapshot; // 현재잔액의 snapshot(매번 실제 거래 후 잔액을 계산하지 않고 거래 후 바로 보여주기 위해 필요)

    // transactionId : 트랜잭션 전용 id
    //                 pk값인 id가 있지만 사용자가 의도해 만들 수 있는 값이 아님.
    //                 또한 id를 확인하면 현재 거래 건 수가 몇 건인지
    //                 사업적인 정보가 노출되기 때문에 또 다른 pk값이 존재하는 것이다.
    //                 (즉, 내부적인 api 호출에는 id를 사용할 수 있지만 외부에서는 transactionId를 사용한다.)
    //                 (외부에서 id값은 절대 사용하지 않는 것이 해킹예방에도 좋다.)
    private String transactionId;
    // transactedAt : 거래일시
    //                (생성일시, 수정일시는 비지니스의 실제 시간으로 사용하지 않는다.
    //                추후 다른 값을 변경하면서 업데이트 될 수 있기 때문이다.)
    private LocalDateTime transactedAt;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
