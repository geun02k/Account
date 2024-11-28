package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
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

    // main함수가 아니지만 실행가능한 이유 : Junit 프레임워크가 대신 실행해주기 떄문.
    @Test
    void testGetAccount() {
        // given
        // 계좌 생성 : 현재 pk 1부터 자동생성
        accountService.createAccount();

        // when
        Account account = accountService.getAccount(1L);

        // then
        assertEquals("40000", account.getAccountNumber());
        assertEquals(AccountStatus.IN_USE, account.getAccountStatus());
    }
}