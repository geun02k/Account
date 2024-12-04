package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.account.domain.AccountStatus.IN_USE;
import static com.example.account.type.TransactionResultType.S;
import static com.example.account.type.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    private static final long USE_AMOUNT = 1000L;
    private static final long BALANCE = 9000L;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void successUseBalance() {
        // given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Pobi").build();
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000012").build();

        // 1. 사용자 존재여부 확인 mocking
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        // 2. 계좌 존재여부 확인 mocking
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // 3. 신규 거래내역 저장 mocking
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(USE)
                        .transactionResultType(S)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .amount(USE_AMOUNT)
                        .balanceSnapshot(BALANCE)
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        TransactionDto transactionDto = transactionService.useBalance(
                1L, "1000000000", USE_AMOUNT);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(USE_AMOUNT, captor.getValue().getAmount());
        assertEquals(BALANCE, captor.getValue().getBalanceSnapshot());

        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(BALANCE, transactionDto.getBalanceSnapshot()); //잔액
        assertEquals(USE_AMOUNT, transactionDto.getAmount()); //사용금액
    }

    @Test
    @DisplayName("해당유저없음 - 잔액사용실패")
    void useBalanceFailed_UserNotFound() {
        // given
        // 1. 사용자 존재여부 확인 mocking
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1000000000", USE_AMOUNT));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("해당계좌없음 - 잔액사용실패")
    void useBalanceFailed_AccountNotFound() {
        // given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Pobi").build();

        // 1. 사용자 존재여부 확인 mocking
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        // 2. 계좌 존재여부 확인 mocking
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1000000000", USE_AMOUNT));

        // then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }


    @Test
    @DisplayName("계좌소유주다름 - 잔액사용실패")
    void useBalanceFailed_UserAccountUnMatch() {
        // given
        AccountUser pobi = AccountUser.builder()
                .id(12L)
                .name("Pobi").build();
        AccountUser harry = AccountUser.builder()
                .id(13L)
                .name("Harry").build();

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
                () -> transactionService.useBalance(1L, "1000000000", USE_AMOUNT));

        // then
        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("이미해지된계좌 - 잔액사용실패")
    void useBalanceFailed_AlreadyUnregistered() {
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
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1000000000", USE_AMOUNT));

        // then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래금액>잔액 - 잔액사용실패")
    void useBalanceFailed_AmountExceedBalance() {
        // given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Pobi").build();
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(100L)
                .accountNumber("1000000012").build();

        // 1. 사용자 존재여부 확인 mocking
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        // 2. 계좌 존재여부 확인 mocking
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1000000000", USE_AMOUNT));

        // then
        assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE, exception.getErrorCode());
        verify(transactionRepository, times(0)).save(any());
    }


}