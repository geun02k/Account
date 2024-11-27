package com.example.account.repository;

import com.example.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
   JpaRepository<Account, Long>
   JpaRepository 인터페이스 : Entity를 DB에 저장하기 위해서는 JAP에서 제공하는 repository 필요
                            스프링에서 JPA를 손쉽게 사용가능하도록 한다.
   Account : repository가 활용할 Entity
   Long : Entity의 PK type
*/

@Repository // repository타입 빈으로 등록
public interface AccountRepository extends JpaRepository<Account, Long> {

}
