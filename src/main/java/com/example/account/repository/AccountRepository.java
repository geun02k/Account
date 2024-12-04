package com.example.account.repository;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
   JpaRepository<Account, Long>
   JpaRepository 인터페이스 : Entity를 DB에 저장하기 위해서는 JAP에서 제공하는 repository 필요
                            스프링에서 JPA를 손쉽게 사용가능하도록 한다.
   Account : repository가 활용할 Entity
   Long : Entity의 PK type
*/

@Repository // repository타입 빈으로 등록
public interface AccountRepository extends JpaRepository<Account, Long> {
    // 제일 마지막 생성된 계좌번호 조회
    // = id로 내림차순 정렬해 첫번째값 가져옴(가장큰값)
    // - JPA는 이름을 형식에 맞춰 쓰기만하면 자동으로 쿼리 생성해준다.
    //   -> 인터페이스의 구현부는 따로 생성하지 않음.
    //   단, 생성된 계좌가 하나도 없는 경우엔 값이 없을 수 있기 때문에
    //   Optional<Account> 타입으로 return 받음.
    Optional<Account> findFirstByOrderByIdDesc();

    // 해당 유저의 총 계좌 수 조회
    // AccountRepository는 JpaRepository<Account, Long>를 확장한 것이다.
    // 여기서 Account 내부에서 @ManyToOne 어노테이션을 통해 AccountUser를
    // 연관관계로 들고있기 때문에 countByAccountUser의 전달인자로 AccountUser 사용이 가능하다.
    Integer countByAccountUser(AccountUser accountUser);

    // 계좌번호와 일치하는 계좌 조회
    Optional<Account> findByAccountNumber(String AccountNumber);
}
