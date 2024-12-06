package com.example.account.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/** 테이블에서 공통되는 PK, 메타정보 분리해 BaseEntity 생성.
 *  Account, AccountUser, Transaction 같은 domain(테이블 Entity)은 비즈니스 정보만을 담을 수 있다. */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass // BaseEntity로 사용할 때 필요한 필수 어노테이션 (JPA 공부 시 알 수 있음.)
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {
    // 테이블의 PK
    // @Id : 해당 테이블의 PK를 id로 지정.
    @Id
    @GeneratedValue
    private Long id;

    // 테이블의 메타정보
    // JPA에서 createdAt, updatedAt 두 값은 테스트하기 번거롭다.
    // @CreatedDate : 데이터는 자동으로 생성해 저장해주는 기능 제공.
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
