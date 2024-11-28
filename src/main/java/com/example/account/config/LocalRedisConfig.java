package com.example.account.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

// config패키지
// : config파일을 넣기도 하지만 controller, service 등에 넣기 어려운
//   별도의 빈을 생성하는 경우에도 많이 사용.

@Configuration
public class LocalRedisConfig {
    @Value("${spring.redis.port}")
    private int redisPort; // 레디스 프로그램 포트번호

    private RedisServer redisServer;

    // Bean등록 시 Redis 실행
    @PostConstruct
    public void startRedis() {
        // Redis서버 객체생성해 시작
        redisServer = new RedisServer(redisPort);
        redisServer.start();
    }

    // 종료시 bean 종료하면서 Redis 종료
    @PreDestroy
    public void stopRedis() {
        // Redis서버가 잘 생성되었을 때 종료
        if(redisServer != null) {
            redisServer.stop();
        }
    }
}
