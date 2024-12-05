package com.example.account.controller;

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

    @PostMapping("/transaction/use")
    public UseBalance.Response useBalance(
            @Valid @RequestBody UseBalance.Request request){

        try {
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
