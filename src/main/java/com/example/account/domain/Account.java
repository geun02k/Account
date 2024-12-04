package com.example.account.domain;

import com.example.account.exception.AccountException;
import com.example.account.type.ErrorCode;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

// @Entity : JPA를 사용해 테이블과 매핑할 클래스에 붙여주는 어노테이션.
//           (해당 클래스는 일반적인 클래스가 아닌 테이블이 설정 클래스)
// @Builder : @Builder로 객체 생성 시 빌더가 들어간 객체를 상속받았을 때도 문제없이 동작을 위해
//            NoArgsConstructor, AllArgsConstructor 존재필수.
// @EntityListeners(AuditingEntityListener.class)
// : @CreatedDate 어노테이션을 동작하기위해 추가
//   @EntityListeners 어노테이션을 사용하기 위해서는 설정도 추가필요.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Account {
    // @Id : Account테이블의 PK를 id로 지정.
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne // 계좌-유저는 n:1의 관계
    private AccountUser accountUser; //(user 사용하면 DB의 user테이블과 혼동 또는 예약어라 문제 발생여지 있으므로 변경)

    private String accountNumber;

    // @Enumerated(EnumType.STRING)
    // : Enum값에 숫자를 저장하는 게 아니라 입력한 문자열이름을 그대로 DB에 저장 (ORDINAL는 숫자저장)
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    private Long balance;

    private LocalDateTime registeredAt;

    private LocalDateTime unRegisteredAt;

    // JPA에서 createdAt, updatedAt 두 값은 테스트하기 번거롭다.
    // @CreatedDate : 데이터는 자동으로 생성해 저장해주는 기능 제공.
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 잔액을 변경하는 일 = 증요 데이터를 변경하는 일 -> 위험
    // 따라서 객체에 안에서 로직을 처리할 수 있도록
    // 잔액변경메서드를 포함시키는 것이 안전한 방법일 수 있다.
    // 또한 service단에서 잔액을 가져오고 값을 변경할 필요없이 해당 메서드 호출로 해결가능.
    public void useBalance(Long amount) {
        if(amount > balance) {
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }
        balance -= amount;
    }

    public void cancelBalance(Long amount) {
        if(amount < 0) {
            throw new AccountException(ErrorCode.INVALID_REQUEST);
        }
        balance += amount;
    }

}

/*
 Account Entity 생성 후 애플리케이션 실행 시 아래와같이 쿼리문 실행되며 테이블생성.

      create table account (
       id bigint not null,
        account_number varchar(255),
        account_status varchar(255),
        primary key (id)
    )
*/