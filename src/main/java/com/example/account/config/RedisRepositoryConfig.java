package com.example.account.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisRepositoryConfig {
    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Bean // Bean의 name 속성 미설정 시 메서드명이 빈이름.
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + redisHost + ":" + redisPort);

        // redisClient 반환
        // : config 설정을 사용해 Redis 생성해 빈으로 등록.
        //   다른 클래스에서 redissonClient를 주입받게되면 이 때 생성된 하나의 빈을 호출해 사용.
        return Redisson.create(config);
    }
}
