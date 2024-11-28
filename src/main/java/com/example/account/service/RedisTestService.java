package com.example.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTestService {
    // RedisClient 객체 빈 자동 생성자주입.
    // 필드명이 빈으로 등록된 메서드명과 동일한 경우 해당 메서드를 통해 생성된 빈 자동주입.
    // RedisRepositoryConfig.java의 redissonClient()
    private final RedissonClient redissonClient;

    public String getLock() {
        // Lock 가져오기
        RLock lock = redissonClient.getLock("sampleLock");

        try{
            // Lock 점유 시도
            // waitTime : 최대 1초 동안 해당 락을 찾아보기.
            // leaseTime : Lock 점유 3초 후 반납.
            //             (명시적으로 unlock()하지 않은 경우에 3초후 자동 점유해제)
            boolean isLock = lock.tryLock(1, 5, TimeUnit.SECONDS);

            // lock 점유 실패
            if(!isLock) {
                log.error("========== Lock acquisition failed ==========");
                return "Lock failed";
            }
        } catch (Exception e) {
            log.error("Lock failed");
        }

//        // 명시적으로 lock 점유 해제.
//        lock.unlock();

        return "Lock success";
    }
}
