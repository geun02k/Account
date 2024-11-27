package com.example.account.controller;

import com.example.account.domain.Account;
import com.example.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

// 레이어 아키텍쳐
// : 외부에서는 controller에만 접근,
//   controller는 service에만 접근,
//   service는 repository에 접근하는 계층화된 구조

@RestController // controller타입 빈으로 등록
@RequiredArgsConstructor
public class AccountController {
    // 의존성주입 -> controller는 service에만 의존
    private final AccountService accountService;

    // createAccount API 생성
    @GetMapping("/create-account")
    public String createAccount() {
        accountService.createAccount();
        return "success";
    }

    // getAccount API 생성
    @GetMapping("/account/{id}")
    public Account getAccount(@PathVariable Long id) {
        return accountService.getAccount(id);
    }

}


