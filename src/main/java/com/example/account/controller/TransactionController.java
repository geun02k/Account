package com.example.account.controller;

import com.example.account.dto.CancelBalance;
import com.example.account.dto.UseBalance;
import com.example.account.exception.AccountException;
import com.example.account.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    // 파라미터 :  거래아이디, 계좌번호, 취소요청금액
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

}
