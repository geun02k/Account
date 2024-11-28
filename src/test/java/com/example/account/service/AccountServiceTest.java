
package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

// @SpringBootTest
// : org.springframework.boot:spring-boot-starter-test 에 포함된 기능
// : 의존성을 주입하지 않고 테스트가능
// springboot context loader를 실제 어플리케이션처럼 테스트용으로 만들어줌.
// context(설정 등)를 실제 환경과 동일하게 모든 기능을 생성해 빈들을 등록한 것을 이용해 테스트 가능.
// 따라서 의존성 주입을 이용해 테스트가능.
// private AccountService accountService = new AccountService(new AccountRepository());와 같이 직성하지않고
// @Autowired
// private AccountService accountService;

// @SpringBootTest -> @ExtendWith(MockitoExtension.class)
// Junit을 그대로 이용해서는 Mockito 기능 사용불가.
// Mockito 확장팩을 테스트 클래스에 달아줌.
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    // accountService는 AccountRepository에 의존하고있다.
    // AccountRepository를 가짜로 생성해 AccountService에 의존성을 추가해준다.

    // @Mock
    // : Mockito 라이브러리에서 제공하는 어노테이션
    // : AccountRepository를 가짜로 생성해 accountRepository에 담아준다.
    //   (injection, 의존성주입과 비슷하게 생성해준다.)
    @Mock
    private AccountRepository accountRepository;

    // Mock으로 생성해준 accountRepository를 accountService에 inject.
    @InjectMocks
    private AccountService accountService;

    // main함수가 아니지만 실행가능한 이유 : Junit 프레임워크가 대신 실행해주기 떄문.
    /**
        Mock을 사용해 테스트하면
        동일 테스트코드를 함께 실행해도 하나는 성공하고 하나는 실패하는 문제 해결가능.
        DB에 데이터가 바뀌든 의존하고 있는 repository의 로직이 변경되든 관계없이
        내가 맡은 역할만 테스트가능.
     */
    @Test
    void testGetAccount() {
        // findById() 메서드 반환값은 Optional이므로 Optional타입의 Account를 반환해준다.
        // given
        given(accountRepository.findById(anyLong()))
                .willReturn(Optional.of(Account.builder()
                        .accountNumber("65789")
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .build()));

        // when
        Account account = accountService.getAccount(4555L);

        // then
        assertEquals("65789", account.getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatus());
    }

    @Test
    void testGetAccount2() {
        given(accountRepository.findById(anyLong()))
                .willReturn(Optional.of(Account.builder()
                        .accountNumber("65789")
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .build()));

        // when
        Account account = accountService.getAccount(4555L);

        // then
        assertEquals("65789", account.getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatus());
    }

}