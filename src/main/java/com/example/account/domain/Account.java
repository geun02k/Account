package com.example.account.domain;

import lombok.*;

import javax.persistence.*;

// @Entity : JPA를 사용해 테이블과 매핑할 클래스에 붙여주는 어노테이션.
//           (해당 클래스는 일반적인 클래스가 아닌 테이블이 설정 클래스)
// @Builder : @Builder로 객체 생성 시 빌더가 들어간 객체를 상속받았을 때도 문제없이 동작을 위해
//            NoArgsConstructor, AllArgsConstructor 존재필수.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Account {
    // @Id : Account테이블의 PK를 id로 지정.
    @Id
    @GeneratedValue
    private Long id;

    private String accountNumber;

    // @Enumerated(EnumType.STRING)
    // : Enum값에 숫자를 저장하는 게 아니라 입력한 문자열이름을 그대로 DB에 저장 (ORDINAL는 숫자저장)
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;
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