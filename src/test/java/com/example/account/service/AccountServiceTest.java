
package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest
// : org.springframework.boot:spring-boot-starter-test 에 포함된 기능
// : 의존성을 주입하지 않고 테스트가능
// springboot context loader를 실제 어플리케이션처럼 테스트용으로 만들어줌.
// context(설정 등)를 실제 환경과 동일하게 모든 기능을 생성해 빈들을 등록한 것을 이용해 테스트 가능.
// 따라서 의존성 주입을 이용해 테스트가능.
// private AccountService accountService = new AccountService(new AccountRepository());와 같이 직성하지않고
// @Autowired
// private AccountService accountService;
@SpringBootTest
class AccountServiceTest {
    @Autowired
    private AccountService accountService;

    // 전체 테스트 메서드 수행 전 수행 (계좌생성)
    @BeforeEach
    void init() {
        accountService.createAccount();
    }

    // main함수가 아니지만 실행가능한 이유 : Junit 프레임워크가 대신 실행해주기 떄문.
    @Test
    void testGetAccount() {
        // when
        Account account = accountService.getAccount(1L);

        // then
        assertEquals("40000", account.getAccountNumber());
        assertEquals(AccountStatus.IN_USE, account.getAccountStatus());
    }

    // @BeforeEach가 각 메서드 수행전에 동작하기 때문에 testGetAccount2()를 수행하는 경우 성공하고,
    // testGetAccount() 메서드의 getAccount(2L)로 했을 때는 실패한다.
    // 동일 테스트코드인데 어떤 것은 성공하고 어떤 것은 실패하게 되는 것이다.
    // 이를 해결하기위해 Mocking을 이용한다.
//    @Test
//    void testGetAccount2() {
//        // when
//        Account account = accountService.getAccount(2L);
//
//        // then
//        assertEquals("40000", account.getAccountNumber());
//        assertEquals(AccountStatus.IN_USE, account.getAccountStatus());
//    }
}