package com.example.account.controller;

import com.example.account.aop.AccountLock;
import com.example.account.dto.CancelBalance;
import com.example.account.dto.QueryTransactionResponse;
import com.example.account.dto.UseBalance;
import com.example.account.exception.AccountException;
import com.example.account.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 잔액 관련 controller
 * 1. 잔액 사용
 * 2. 잔액 사용 취소
 * 3. 거래 확인
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    // @AccountLock
    // 동시성 이슈 해결을 위해 거래 시 Lock을 걸고 해제하도록 함.
    // 하지만 단지 어노테이션만 붙여준다고 어떤 기능을 수행하는 것은 아니다.
    // 어노테이션이 달려있는 부분에서 동작을 하게되는 AOP Aspect를 만들어 동시성 제어를 하도록 한다.
    @PostMapping("/transaction/use")
    @AccountLock // 동시성 이슈 해결을 위해 거래 시 Lock을 걸고 해제하도록 함.
    public UseBalance.Response useBalance(
            @Valid @RequestBody UseBalance.Request request) throws InterruptedException {

        try {
            // 거래중복방지 AOP 추가 후 Thread.sleep(5000L); 호출시 아래의 에러발생
            //java.lang.IllegalMonitorStateException:
            // attempt to unlock lock, not locked by current thread by node id: 350e1544-5ada-4f5c-80b2-b60931adae57 thread-id: 78
            // 아직 Lock이 점유되지 않았는데 해제되어 문제발생.
            // Thread.sleep(3000L); 으로 변경하고
            // LockService.java 의 lock.tryLock(1, 5, TimeUnit.SECONDS) 부분에서 5->15로 변경
            Thread.sleep(3000L);
            return UseBalance.Response.from(transactionService.useBalance(
                    request.getUserId(),
                    request.getAccountNumber(),
                    request.getAmount()));

        } catch (AccountException e) {
            // 비즈니스 적으로 의도적으로 만든 exception이 발생했을 때는
            // 에러로그 출력 및 DB에 실패거래정보 저장
            log.error("Failed to use balance.");
            transactionService.saveFailedUseTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );

            throw e;
        }
    }

    @PostMapping("/transaction/cancel")
    @AccountLock
    public CancelBalance.Response cancelBalance(
            @Valid @RequestBody CancelBalance.Request request){

        try {
            return CancelBalance.Response.from(transactionService.cancelBalance(
                    request.getTransactionId(),
                    request.getAccountNumber(),
                    request.getAmount()));

        } catch (AccountException e) {
            // 비즈니스 적으로 의도적으로 만든 exception이 발생했을 때는
            // 에러로그 출력 및 DB에 실패거래정보 저장
            log.error("Failed to cancel balance.");
            // 반환 : 계좌번호, 거래결과코드(성공/실패), 거래아이디, 거래금액, 거래일
            transactionService.saveFailedCancelTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );

            throw e;
        }
    }

    // 실제 거래시에도 거래확인api를 항상 제공한다.
    // 거래를 했는데 타임아웃이 발생하는 경우도 있다. (네트워크 이슈 또는 클라이언트의 딜레이 등의 문제 발생가능)
    // 이런 경우 클라이언트가 정상적인 거래 응답을 받지 못할 수 있다.
    @GetMapping("/transaction/{transactionId}")
    public QueryTransactionResponse queryTransaction(
            @PathVariable String transactionId) {

        return QueryTransactionResponse.from(
                transactionService.queryTransaction(transactionId)
        );
    }
}
