
package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    // accountRepository, accountUserRepository 두 개의 mock을 담는 accountService를 생성
    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccountSuccess() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi").build();
        // 1. 사용자 존재여부 확인 mocking
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        // 2. 계좌번호 생성 mocking
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                                .accountNumber("1000000012").build()));
        // 생성되어야 할 계좌번호 = 마지막 계좌번호 + 1이 되어야함을 확인
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // 3. 계좌 저장 mocking
        // 외부 의존성에 있는 accountRepository가 응답을 주지 않을 때 null 반환 -> 테스트 시 save 메서드에 대해서도 mocking필요
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000013").build());

        // when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        // 현재 계좌번호가 마지막계좌번호인 1000000012 +1인지 확인
        assertEquals("1000000013", captor.getValue().getAccountNumber());
    }

    @Test
    @DisplayName("계좌번호 저장내역없음")
    void createFirstAccount() {
        // given
        // 1. 사용자 존재여부 확인 mocking
        // 현재 mocking에서 에러발생으로 다음로직의 mocking들은 의미없음 -> 제거
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        // createAccount() 메서드 실행 시 AccountException 발생.
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        // then
        // USER_NOT_FOUND의 에러코드 반환하는지 확인.
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("해당유저없음 - 계좌생성실패")
    void createAccount_UserNotFount() {
        // given
        AccountUser user = AccountUser.builder()
                .id(15L)
                .name("Pobi").build();
        // 1. 사용자 존재여부 확인 mocking
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        // 2. 계좌번호 생성 mocking
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());
        // 생성되어야 할 계좌번호 = 마지막 계좌번호 + 1이 되어야함을 확인
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // 3. 계좌 저장 mocking
        // 외부 의존성에 있는 accountRepository가 응답을 주지 않을 때 null 반환 -> 테스트 시 save 메서드에 대해서도 mocking필요
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000013").build());

        // when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(15L, accountDto.getUserId());
        // 현재 계좌번호가 마지막계좌번호인 1000000012 +1인지 확인
        assertEquals("1000000000", captor.getValue().getAccountNumber());
    }

    @Test
    @DisplayName("유저당 최대 계좌 수인 10개 초과 - 계좌생성실패")
    void createAccount_maxAccountIs10() {
        // given
        AccountUser user = AccountUser.builder()
                .id(15L)
                .name("Pobi").build();
        // 1. 사용자 존재여부 확인 mocking
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        // 2. 사용자의 계좌 수 확인 mocking
        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        // then
        assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, exception.getErrorCode());
    }

    @Test
    void deleteAccountSuccess() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi").build();

        // 1. 사용자 존재여부 확인 mocking
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        // 2. 계좌 존재여부 확인 mocking
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto = accountService.deleteAccount(1L, "1000000002");

        // then
        //사용자아이디, 계좌번호, 계좌상태 return값 확인
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000012", captor.getValue().getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, captor.getValue().getAccountStatus());
    }

}
