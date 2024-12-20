
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

import java.util.Arrays;
import java.util.List;
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
                .name("Pobi").build();
        user.setId(12L);
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
    @DisplayName("해당유저없음 - 계좌생성실패")
    void createAccount_UserNotFount() {
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
    @DisplayName("계좌번호 저장내역없음")
    void createFirstAccount() {
        // given
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(15L);
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
                .name("Pobi").build();
        user.setId(15L);
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
                .name("Pobi").build();
        user.setId(12L);

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

    @Test
    @DisplayName("해당유저없음 - 계좌해지실패")
    void deleteAccountFailed_UserNotFound() {
        // given
        // 1. 사용자 존재여부 확인 mocking
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("해당계좌없음 - 계좌해지실패")
    void deleteAccountFailed_AccountNotFound() {
        // given
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(12L);

        // 1. 사용자 존재여부 확인 mocking
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        // 2. 계좌 존재여부 확인 mocking
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        // then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌소유주다름 - 계좌해지실패")
    void deleteAccountFailed_UserAccountUnMatch() {
        // given
        AccountUser pobi = AccountUser.builder()
                .name("Pobi").build();
        AccountUser harry = AccountUser.builder()
                .name("Harry").build();
        pobi.setId(12L);
        harry.setId(13L);

        // 1. 사용자 존재여부 확인 mocking
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        // 2. 계좌 존재여부 확인 mocking
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(harry)
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        // then
        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("해지계좌잔액있음 - 계좌해지실패")
    void deleteAccountFailed_BalanceNotEmpty() {
        // given
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(12L);

        // 1. 사용자 존재여부 확인 mocking
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        // 2. 계좌 존재여부 확인 mocking
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .balance(100L)
                        .accountNumber("1000000012").build()));

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        // then
        assertEquals(ErrorCode.BALANCE_NOT_EMPTY, exception.getErrorCode());
    }

    @Test
    @DisplayName("이미해지된계좌 - 계좌해지실패")
    void deleteAccountFailed_AlreadyUnregistered() {
        // given
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(12L);

        // 1. 사용자 존재여부 확인 mocking
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        // 2. 계좌 존재여부 확인 mocking
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        // then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }

    @Test
    void successGetAccountsByUserId() {
        // given
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(1L);
        List<Account> accounts = Arrays.asList(
                Account.builder()
                        .accountUser(user)
                        .accountNumber("1111111111")
                        .balance(1000L)
                        .build(),
                Account.builder()
                        .accountUser(user)
                        .accountNumber("2222222222")
                        .balance(2000L)
                        .build()
        );
        // 1. 사용자 존재여부 확인 mocking
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        // 2. 사용자의 계좌목록 조회 mocking
        given(accountRepository.findByAccountUser(any()))
                .willReturn(accounts);

        // when
        List<AccountDto> accountDtos = accountService.getAccountsByUserId(1L);

        // then
        assertEquals(2, accountDtos.size());
        assertEquals("1111111111", accountDtos.get(0).getAccountNumber());
        assertEquals(1000, accountDtos.get(0).getBalance());
        assertEquals("2222222222", accountDtos.get(1).getAccountNumber());
        assertEquals(2000, accountDtos.get(1).getBalance());
    }

    @Test
    @DisplayName("해당유저없음 - 계좌조회실패")
    void getAccountsFailed_UserNotFound() {
        // given
        // 1. 사용자 존재여부 확인 mocking
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.getAccountsByUserId(1L));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

}
