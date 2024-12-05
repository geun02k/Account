package com.example.account.service;

import com.example.account.exception.AccountException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.example.account.type.ErrorCode.ACCOUNT_TRANSACTION_LOCK;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockService {
    // RedisClient 객체 빈 자동 생성자주입.
    // 필드명이 빈으로 등록된 메서드명과 동일한 경우 해당 메서드를 통해 생성된 빈 자동주입.
    // RedisRepositoryConfig.java의 redissonClient()
    private final RedissonClient redissonClient;

    /** Lock 점유 */
    public void lock(String accountNumber) {
        // Lock 가져오기 (Lock의 키는 계좌번호로 사용)
        // redissonClient.getLock("ACLK:" + accountNumber);
        // : getLock() 메서드의 인자로 "ACLK:" + accountNumber를 전달하면 의미가 모호하기 때문에
        //   private 메서드로 뽑아내 명시적으로 표현한다.
        RLock lock = redissonClient.getLock(getLockKey(accountNumber));
        log.debug("Trying lock for accountNumber : {}" + accountNumber);

        try{
            // Lock 점유 시도
            // waitTime : Lock을 취득하는 데 기다리는 시간(최대 1초 동안 해당 락을 찾아보기)
            // leaseTime : Lock 점유 해제하는데 걸리는 시간 (Lock 점유 5초 후 반납)
            //             (아무동작도 하지 않으면 명시적으로 unlock()하지 않은 경우에 5초후 자동 점유해제)
            boolean isLock = lock.tryLock(1, 15, TimeUnit.SECONDS);
            // lock 점유 실패
            if(!isLock) {
                log.error("========== Lock acquisition failed ==========");
                throw new AccountException(ACCOUNT_TRANSACTION_LOCK);
            }
            // throw new AccountException(ACCOUNT_TRANSACTION_LOCK);를 통해 발생시킨 에러를
            // ExceptionHandler가 예외를 받아 처리할 수 있도록 하기위해
            // catch (AccountException e) 사용.
        } catch (AccountException e) { // Lock을 가져오지 못했을 때 발생하는 에러
            throw e;
        } catch (Exception e) { // Lock을 가져오지 못했을 때 발생하는 에러 이외의 예상치못한 에러
            log.error("Redis lock failed", e);
        }
    }

    /** Lock 해제 */
    public void unlock(String accountNumber) {
        // 명시적으로 lock 점유 해제.
        // lock.unlock();
        log.debug("Unlock for accountNumber : {}" + accountNumber);
        redissonClient.getLock(getLockKey(accountNumber)).unlock();
    }

    private static String getLockKey(String accountNumber) {
        return "ACLK:" + accountNumber;
    }
}
