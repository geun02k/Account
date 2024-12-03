
package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

}
