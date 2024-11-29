package com.example.account.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// @Configuration
// : @Component의 일종으로 자동으로 빈으로 등록.

// @EnableJpaAuditing
// : 스프링 어플리케이션 쓸 때 이 설정파일이 auto scan되는 타입으로 설정.
// : db에 저장되거나 업데이트 될 때 @CreatedDate, @LastModifiedDate 어노테이션이 사용된 필드에 대해 값을 자동저장.

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration { // 자동 회계 관련 어노테이션 설정

}
