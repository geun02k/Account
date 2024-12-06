package com.example.account.service;

import com.example.account.dto.UseBalance;
import com.example.account.exception.AccountException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static com.example.account.type.ErrorCode.ACCOUNT_TRANSACTION_LOCK;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LockAopAspectTest {
    @Mock
    private LockService lockService;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

     @InjectMocks
    private LockAopAspect lockAopAspect;

     @Test
     @DisplayName("정상적 lock(), unlock(), 수행")
     void lockAndUnlock() throws Throwable {
         // given
         String accountNumber = "1000000000";
         ArgumentCaptor<String> lockArgumentCaptor =
                 ArgumentCaptor.forClass(String.class);
         ArgumentCaptor<String> unlockArgumentCaptor =
                 ArgumentCaptor.forClass(String.class);
         UseBalance.Request request =
                 new UseBalance.Request(123L, accountNumber, 1000L);

         // when
         lockAopAspect.aroundMethod(proceedingJoinPoint, request);

         // then
         verify(lockService, times(1))
                 .lock(lockArgumentCaptor.capture());
         verify(lockService, times(1))
                 .unlock(unlockArgumentCaptor.capture());
         assertEquals(accountNumber, lockArgumentCaptor.getValue());
         assertEquals(accountNumber, unlockArgumentCaptor.getValue());
     }

    @Test
    @DisplayName("도중에 예외가 발생해도 lock(), unlock(), 수행")
    void lockAndUnlock_evenIfThrow() throws Throwable {
        // given
        String accountNumber = "1000000000";
        ArgumentCaptor<String> lockArgumentCaptor =
                ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> unlockArgumentCaptor =
                ArgumentCaptor.forClass(String.class);
        UseBalance.Request request =
                new UseBalance.Request(123L, accountNumber, 1000L);

        given(proceedingJoinPoint.proceed())
                .willThrow(new AccountException(ACCOUNT_TRANSACTION_LOCK));

        // when
        assertThrows(AccountException.class, () ->
            lockAopAspect.aroundMethod(proceedingJoinPoint, request));

        // then
        verify(lockService, times(1))
                .lock(lockArgumentCaptor.capture());
        verify(lockService, times(1))
                .unlock(unlockArgumentCaptor.capture());
        assertEquals(accountNumber, lockArgumentCaptor.getValue());
        assertEquals(accountNumber, unlockArgumentCaptor.getValue());
    }
}