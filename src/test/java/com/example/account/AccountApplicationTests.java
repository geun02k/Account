package com.example.account;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// 스프링 어플리케이션이 뜨는지를 테스트해주는 기본적으로 내장되어있는 test코드
// 서버가 실행중일 떄 테스트코드를 실행하면 redis 포트를 사용하고있다고 에러가 발생한다.
// 그래서 전체 테스트 시 AccountApplicationTests이 포함되어있고 서버가 실행중이라면
// 스프링 어플리케이션 서버를 종료해야 AccountApplicationTests 에러가 발생하지 않는다.
@SpringBootTest
class AccountApplicationTests {

    @Test
    void contextLoads() {
    }

}
