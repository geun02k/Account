package com.example.account.aop;

import java.lang.annotation.*;

/**
 잔액변경같은 일을 수행할 때는 동시 접근하는 일을 방지해주어야 한다.

 우리는 redis라는 외부 자원을 이용해 동시성을 제어한다.
 @AccountLock 이라는 커스텀 어노테이션을 이용해 TransactionController 중
 동시성제어가 필요한 컨트롤러에 Lock을 걸고 로직을 수행하도록 한다.

 동시성이슈(concurrency issue)
 : 여러 요청이 동일한 자원에 접근하여 발생하는 문제들을 통칭.
   주로 DB에서 동일한 레코드를 동시 접근하며 문제 발생.

 해결방법
 1. DB의존적 방법
    : 과거에 많이 사용, isolate 성능은 떨어지지만 동시성이슈에서 가장 자유로움.
      JPA에서 지원하는 낙관적lock, 비관적lock 등의 방법 존재.
      DB 자원은 한정적이고 서버는 늘리기 쉽지만 DB는 늘리기 어렵다.
      따라서 DB의존적 방법은 성능상 떨어질 수 밖에 없다.
      또한 DB에 의존하다 보면 코드도 DB에 의지해야하니 코드의 자율성도 떨어지고 성능도 떨어진다.
 2. 기타 인프라를 활용하는 방법
    : Redis를 이용해 메모리에 저장하는 방식으로 많이 사용.
      따라서 Rdis에서 lock을 걸어 처리한다.
      (lock을 획득하면 처리, lock을 획득하지 못하면 이후로직 진행x)
      성능이 상당히 빠르다.
 3. 비즈니스 로직으로 해결하는 방법
    : 예를 들면 모든 요청을 다 받고 결과를 1시간 뒤에 알려주는 방식으로
      정책을 통해 해결한다.
      요청을 모두 등록해놓고 요청이 등록된 시간이 빠른 것부터 성공으로 결과를 리턴해준다.
 */

@Target(ElementType.METHOD) // 해당 어노테이션을 사용할 수 있는 target -> 메소드로 설정.
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited // 상속가능한 구조로 사용.
public @interface AccountLock {
    // 일정시간동안 Lock을 기다리는 메서드
    long tryLockTime() default 5000L;
}
