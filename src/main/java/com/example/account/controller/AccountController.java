package com.example.account.controller;

import com.example.account.domain.Account;
import com.example.account.dto.AccountDto;
import com.example.account.dto.CreateAccount;
import com.example.account.dto.DeleteAccount;
import com.example.account.service.AccountService;
import com.example.account.service.RedisTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

// 레이어 아키텍쳐
// : 외부에서는 controller에만 접근,
//   controller는 service에만 접근,
//   service는 repository에 접근하는 계층화된 구조

@RestController // controller타입 빈으로 등록
@RequiredArgsConstructor
public class AccountController {
    // 의존성주입 -> controller는 service에만 의존
    private final AccountService accountService;
    private final RedisTestService redisTestService;

    // Redis Lock Test API 생성
    @GetMapping("/get-lock")
    public String getLock() {
        return redisTestService.getLock();
    }

    // 계좌생성 API 호출
    @PostMapping("/account")
    public CreateAccount.Response createAccount(
            @RequestBody @Valid CreateAccount.Request request) {
        return CreateAccount.Response.from(
                accountService.createAccount(
                    request.getUserId(),
                    request.getInitialBalance()
                )
        );
    }

    // 계좌해지 API 호출
    @DeleteMapping("/account")
    public DeleteAccount.Response deleteAccount(
            @RequestBody @Valid DeleteAccount.Request request) {
        return DeleteAccount.Response.from(
                accountService.deleteAccount(
                        request.getUserId(),
                        request.getAccountNumber()
                )
        );
    }

    // getAccount API 생성
    @GetMapping("/account/{id}")
    public Account getAccount(@PathVariable Long id) {
        return accountService.getAccount(id);
    }

}


