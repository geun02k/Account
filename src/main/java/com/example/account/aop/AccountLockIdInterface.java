package com.example.account.aop;

// CancelBalance.Request, UseBalance.Request 두 객체를 공통화하기 위해 사용.

// AOP를 사용하는 부분은 거래가 발생하는 부분이다. -> cancelBalance(), useBalance()
// cancelBalance(), useBalance()의 요청타입은 CancelBalance.Request, UseBalance.Request로 두 타입이 다르다.
// arg(request)로 받을 두 객체의 타입이 상이하므로 이를 공통으로 처리하기 위한 타입이 따로 필요하다.
// 하지만 응답값이 다른 두 메서드에 대해 동일한 응답타입을 사용하는 것은 권장하지 않는다.
// 따라서 두 타입을 공통화하기 위해 interface를 사용한다.

public interface AccountLockIdInterface {
    String getAccountNumber();
}
