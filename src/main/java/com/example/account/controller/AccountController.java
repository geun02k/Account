package com.example.account.controller;

import com.example.account.domain.Account;
import com.example.account.dto.AccountInfo;
import com.example.account.dto.CreateAccount;
import com.example.account.dto.DeleteAccount;
import com.example.account.service.AccountService;
import com.example.account.service.RedisTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/account")
    public List<AccountInfo> getAccountByUserId(
            @RequestParam("user_id") Long userId) {
        // List<AccountDto> -> List<AccountInfo> 타입으로 변경
        // stream으로 변환해 map을 통해 처리하는 것은 성능상 떨어진다.
        // 하지만 간단히 타입을 변환하기 위해 사용함.
        // (모든것을 성능 위주로 코드를 짜게되면 코드 복잡도가 올라가고 유지보수가 어려워질 수 있다.
        // 따라서 성능상 문제가 생기지 않는 경우까지는 성능에 대해 많은 신경을 쓰지 않아도 된다.)
        return accountService.getAccountsByUserId(userId)
                .stream().map(accountDto ->
                        AccountInfo.builder()
                        .accountNumber(accountDto.getAccountNumber())
                        .balance(accountDto.getBalance())
                        .build())
                .collect(Collectors.toList());
    }

    // getAccount API 생성
    @GetMapping("/account/{id}")
    public Account getAccount(@PathVariable Long id) {
        return accountService.getAccount(id);
    }

    // Redis Lock Test API 생성
    @GetMapping("/get-lock")
    public String getLock() {
        return redisTestService.getLock();
    }
}


