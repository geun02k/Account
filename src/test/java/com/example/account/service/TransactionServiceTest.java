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
import static com.example.account.type.TransactionResultType.F;
import static com.example.account.type.TransactionResultType.S;
import static com.example.account.type.TransactionType.CANCEL;
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
    private static final long CANCEL_AMOUNT = 1000L;

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
                .name("Pobi").build();
        user.setId(1L);
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
                .name("Pobi").build();
        user.setId(1L);

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
                () -> transactionService.useBalance(1L, "1000000000", USE_AMOUNT));

        // then
        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("이미해지된계좌 - 잔액사용실패")
    void useBalanceFailed_AlreadyUnregistered() {
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
                () -> transactionService.useBalance(1L, "1000000000", USE_AMOUNT));

        // then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래금액>잔액 - 잔액사용실패")
    void useBalanceFailed_AmountExceedBalance() {
        // given
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(1L);
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

    @Test
    void successSaveFailedUseTransaction() {
        // given
        final Long ORG_BALANCE = 10000L;
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(1L);
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(ORG_BALANCE)
                .accountNumber("1000000012").build();

        // 1. 계좌 존재여부 확인 mocking
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // 2. 신규 거래내역 저장 mocking
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
        // 리턴값이 없기 때문에 결과확인불가.
        // -> transactionRepository.save() 에서 저장할 떄 담기는 Entity의 정보를 캡쳐해
        //    해당 정보를 확인해야 함.
        transactionService.saveFailedUseTransaction("1000000000", USE_AMOUNT);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(USE_AMOUNT, captor.getValue().getAmount());
        assertEquals(ORG_BALANCE, captor.getValue().getBalanceSnapshot()); // 잔액 == 초기잔액
        assertEquals(F, captor.getValue().getTransactionResultType());
    }

    @Test
    void successCancelBalance() {
        // given
        AccountUser user = AccountUser.builder() // 사용자추가
                .name("Pobi").build();
        user.setId(1L);
        Account account = Account.builder() // 계좌생성
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L) // 기존잔액
                .accountNumber("1000000012").build();
        Transaction transaction = Transaction.builder() // 잔액사용
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionIdForCancel")
                .transactedAt(LocalDateTime.now())
                .amount(USE_AMOUNT)
                .balanceSnapshot(BALANCE)
                .build();

        // 1. 거래내역 존재여부 확인 mocking
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        // 2. 계좌 존재여부 확인 mocking
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // 3. 신규 거래내역 저장 mocking
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(CANCEL)
                        .transactionResultType(S)
                        .transactionId("transactionIdForCancel")
                        .transactedAt(LocalDateTime.now())
                        .amount(CANCEL_AMOUNT)
                        .balanceSnapshot(10000L)
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        TransactionDto transactionDto = transactionService.cancelBalance(
                "transactionIdForCancel", "1000000000", CANCEL_AMOUNT);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(CANCEL_AMOUNT, captor.getValue().getAmount());
        assertEquals(10000L + CANCEL_AMOUNT, captor.getValue().getBalanceSnapshot());

        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(CANCEL, transactionDto.getTransactionType());
        assertEquals(10000L, transactionDto.getBalanceSnapshot()); //잔액
        assertEquals(CANCEL_AMOUNT, transactionDto.getAmount()); //사용취소금액
    }

    @Test
    @DisplayName("해당사용거래내역없음 - 잔액사용취소실패")
    void cancelBalanceFailed_TransactionNotFound() {
        // given
        // 1. 거래내역 존재여부 확인 mocking
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId", "1000000000", USE_AMOUNT));

        // then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("해당계좌없음 - 잔액사용취소실패")
    void cancelBalanceFailed_AccountNotFound() {
        // given
        // 1. 거래내역 존재여부 확인 mocking
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(Transaction.builder().build()));

        // 2. 계좌 존재여부 확인 mocking
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId", "1000000000", USE_AMOUNT));

        // then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래와계좌불일치 - 잔액사용취소실패")
    void cancelBalanceFailed_TransactionAccountUnMatch() {
        // given
        AccountUser user = AccountUser.builder() // 사용자추가
                .name("Pobi").build();
        user.setId(1L);
        Account account = Account.builder() // 계좌생성
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L) // 기존잔액
                .accountNumber("1000000012").build();
        account.setId(1L);
        Account accountNotUse = Account.builder() // 계좌생성
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L) // 기존잔액
                .accountNumber("1000000013").build();
        accountNotUse.setId(2L);
        Transaction transaction = Transaction.builder() // 잔액사용
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionIdForCancel")
                .transactedAt(LocalDateTime.now())
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(BALANCE)
                .build();

        // 1. 거래내역 존재여부 확인 mocking
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction)); // transaction은 account 계좌정보사용

        // 2. 계좌 존재여부 확인 mocking
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(accountNotUse)); // accountNotUse 계좌정보사용

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance(
                        "transactionIdForCancel",
                        "1000000000",
                        CANCEL_AMOUNT)
        );

        // then
        assertEquals(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래금액,취소금액 불일치 - 잔액사용취소실패")
    void cancelBalanceFailed_CancelMustFully() {
        // given
        AccountUser user = AccountUser.builder() // 사용자추가
                .name("Pobi").build();
        user.setId(1L);
        Account account = Account.builder() // 계좌생성
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L) // 기존잔액
                .accountNumber("1000000012").build();
        account.setId(1L);
        Transaction transaction = Transaction.builder() // 잔액사용
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionIdForCancel")
                .transactedAt(LocalDateTime.now())
                .amount(CANCEL_AMOUNT + 1000L)
                .balanceSnapshot(BALANCE)
                .build();

        // 1. 거래내역 존재여부 확인 mocking
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        // 2. 계좌 존재여부 확인 mocking
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance(
                        "transactionIdForCancel",
                        "1000000000",
                        CANCEL_AMOUNT)
        );

        // then
        assertEquals(ErrorCode.CANCEL_MUST_FULLY, exception.getErrorCode());
    }

    @Test
    @DisplayName("1년넘은거래취소불가 - 잔액사용취소실패")
    void cancelBalanceFailed_TooOldOrderToCancel() {
        // given
        AccountUser user = AccountUser.builder() // 사용자추가
                .name("Pobi").build();
        user.setId(1L);
        Account account = Account.builder() // 계좌생성
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L) // 기존잔액
                .accountNumber("1000000012").build();
        account.setId(1L);
        Transaction transaction = Transaction.builder() // 잔액사용
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionIdForCancel")
                .transactedAt(LocalDateTime.now().minusYears(1).minusDays(1))
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(BALANCE)
                .build();

        // 1. 거래내역 존재여부 확인 mocking
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        // 2. 계좌 존재여부 확인 mocking
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance(
                        "transactionIdForCancel",
                        "1000000000",
                        CANCEL_AMOUNT)
        );

        // then
        assertEquals(ErrorCode.TOO_OLD_ORDER_TO_CANCEL, exception.getErrorCode());
    }

    @Test
    void successQueryTransaction() {
        // given
        AccountUser user = AccountUser.builder() // 사용자추가
                .name("Pobi").build();
        user.setId(1L);
        Account account = Account.builder() // 계좌생성
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L) // 기존잔액
                .accountNumber("1000000012").build();
        account.setId(1L);
        Transaction transaction = Transaction.builder() // 잔액사용
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now().minusYears(1).minusDays(1))
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(BALANCE)
                .build();

        // 1. 거래내역 존재여부 확인 mocking
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        // when
        TransactionDto transactionDto = transactionService.queryTransaction("trxId");

        // then
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(CANCEL_AMOUNT, transactionDto.getAmount());
        assertEquals("transactionId", transactionDto.getTransactionId());
    }

    @Test
    @DisplayName("해당거래내역없음 - 거래조회실패")
    void queryTransactionFailed_TransactionNotFound() {
        // given
        // 1. 거래내역 존재여부 확인 mocking
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.queryTransaction("transactionId"));

        // then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }

}