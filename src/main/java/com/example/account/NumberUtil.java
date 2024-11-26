package com.example.account;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NumberUtil {
    // static 메서드만 제공하는 유틸리티 성격의 클래스.
    // 생성자를 private으로 선언해 객체생성 불가하도록 설정.
    // @UtilityClass 어노테이션을 이용해 아래의 코드 자동생성
    //private NumberUtil() {}

    public static Integer sum(Integer a, Integer b) {
        return a + b;
    }

    public static Integer minus(Integer a, Integer b) {
        return a- b;
    }
}
