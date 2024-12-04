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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.example.account.type.ErrorCode.*;
import static com.example.account.type.TransactionResultType.F;
import static com.example.account.type.TransactionResultType.S;
import static com.example.account.type.TransactionType.USE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountUserRepository accountUserRepository;
    private final AccountRepository accountRepository;

    /** 잔액 사용
     * 1. 사용자 존재여부 확인
     * 2. 계좌 존재여부 확인
     * 3. 사용자 아이디와 계좌 소유주 일치여부 확인
     *    계좌가 이미 해지 상태인지 확인
     *    거래금액이 잔액보다 큰지 확인
     * 4. 잔액 사용(잔액 변경)
     * 5. 신규 거래내역 저장 및 정보 전달
     * @param userId 사용자ID
     * @param accountNumber 계좌번호
     * @param amount 사용금액
     * @return TransactionDto 거래정보
     */
    @Transactional // service 로직 진행 중 한 군데에서라도 오류가 발생하면 rollback
    public TransactionDto useBalance(Long userId,
                                     String accountNumber,
                                     Long amount) {
        // 1. 사용자 존재여부 확인
        AccountUser user = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
        // 2. 계좌 존재여부 확인
        Account account = accountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));
        // 3. 사용자 아이디와 계좌 소유주 일치여부 확인
        //    계좌가 이미 해지 상태인지 확인
        //    거래금액이 잔액보다 큰지 확인
        validateUseBalance(user, account, amount);

        // 4. 잔액 사용(잔액 변경)
        // 잔액을 가져오고 값을 변경할 필요없이 해당 메서드 호출로 해결가능.
        account.useBalance(amount);
//        Long accountBalance = account.getBalance();
//        account.setBalance(accountBalance - amount);

        // 5. 신규 거래내역 저장 및 정보 전달
        return TransactionDto.fromEntity(transactionRepository.save(
                Transaction.builder()
                        .transactionType(USE)
                        .transactionResultType(S)
                        .account(account)
                        .amount(amount)
                        .balanceSnapshot(account.getBalance())
                        .transactionId(UUID.randomUUID().toString().replace("-", ""))
                        .transactedAt(LocalDateTime.now())
                        .build()
        ));
        // UUID.randomUUID().toString().replace("-", "")
        // : UUID 사용
        //   고유한 값 생성하는 방법 중 가장 검증이 많이되고 편리하고 쉬운 방법
        //   생성된 UUID의 대시(-) 문자를 제거
    }

    private void validateUseBalance(AccountUser user, Account account, Long amount) {
        // 사용자 아이디와 계좌 소유주가 다른 경우
        if(!Objects.equals(user.getId(), account.getAccountUser().getId())) {
            throw new AccountException(USER_ACCOUNT_UN_MATCH);
        }

        // 계좌가 이미 해지 상태인 경우 (사용중인 계좌가 아닌 경우)
        if(account.getAccountStatus() != AccountStatus.IN_USE) {
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }

        // 거래금액이 잔액보다 큰 경우
        if(account.getBalance() < amount) {
            throw new AccountException(AMOUNT_EXCEED_BALANCE);
        }
    }

    @Transactional
    public void saveFailedUseTransaction(String accountNumber, Long amount) {
        // 계좌 미존재시 거래기록 남기지 않음.
        Account account  = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        transactionRepository.save(
                Transaction.builder()
                        .transactionType(USE)
                        .transactionResultType(F) // 실패
                        .account(account)
                        .amount(amount)
                        .balanceSnapshot(account.getBalance())
                        .transactionId(UUID.randomUUID().toString().replace("-", ""))
                        .transactedAt(LocalDateTime.now())
                        .build()
        );
    }
}
