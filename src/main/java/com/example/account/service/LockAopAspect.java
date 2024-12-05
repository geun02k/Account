package com.example.account.service;

import com.example.account.aop.AccountLockIdInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LockAopAspect {
    private final LockService lockService;

    // @Around : 어떤 경우에 Aspect를 적용할지 정의
    // @Around("@annotation(com.example.account.aop.AccountLock)
    // : @AccountLock 어노테이션이 달린 메서드가 실행될 때 Aspect 적용
    // @Around("args(request)")
    // : AccountLock 어노데이터션을 단 메서드의 인자 중
    //   request라는 필드를 가져온다.
    //   'AccountLockIdInterface request'과 같이 동일 이름으로 파라미터를 받아야한다.
    @Around("@annotation(com.example.account.aop.AccountLock) && args(request)")
    public Object aroundMethod(
            ProceedingJoinPoint pjp,
            AccountLockIdInterface request
    ) throws Throwable {

        // before : Lock 취득 시도
        lockService.lock(request.getAccountNumber());
        try {
            // AOP를 걸어줬던 부분 그대로 동작 진행.
            return pjp.proceed();
        } finally {
            // after : Lock 해제
            lockService.unlock(request.getAccountNumber());
        }
    }
}
