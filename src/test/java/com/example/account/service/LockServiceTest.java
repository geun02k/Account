package com.example.account.service;

import com.example.account.exception.AccountException;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LockServiceTest {
    @Mock
    private RedissonClient redissonClient;

    // 우리가 LockService에서 만든 Bean은 아니지만
    // RLock이 동작을 어떻게 해주는지에 따라서 lock(), unlock() 메서드의
    // 로직이 변화하기 때문에 RLock을 mocking해 RLock의 동작을 원하는대로 변경하도록 한다.
    @Mock
    private RLock rLock;

    @InjectMocks
    private LockService lockService;

    @Test
    void successGetLock() throws InterruptedException {
        // given
        given(redissonClient.getLock(anyString()))
                .willReturn(rLock);
        given(rLock.tryLock(anyLong(), anyLong(), any()))
                .willReturn(true); // lock 성공

        // when

        // then
        // 아래의 방식으로 테스트코드를 짜는 것은 권장하지않는다.
        // 하지만 실무적으로 가끔 어쩔 수 없는 경우가 있기도 하다.
        // 따라서 명시적으로 리턴을 주는 방식으로 메서드를 만들어서 할 수도 있을 것 같다.
        assertDoesNotThrow(() -> lockService.lock("1000000000"));
    }

    @Test
    void failGetLock() throws InterruptedException {
        // given
        given(redissonClient.getLock(anyString()))
                .willReturn(rLock);
        given(rLock.tryLock(anyLong(), anyLong(), any()))
                .willReturn(false); // lock 실패

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> lockService.lock("1000000000"));

        // then
        assertEquals(ErrorCode.ACCOUNT_TRANSACTION_LOCK, exception.getErrorCode());
    }
}