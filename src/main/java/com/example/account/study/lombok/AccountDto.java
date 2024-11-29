package com.example.account.study.lombok;

/** Lombok라이브러리 스터디 */

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

// 어노테이션 우클릭 > 리팩터링 > Delombok
// -> 해당 어노테이션으로 인해 추가하지 않아도 되었던 코드들 확인가능.

@Getter
@Setter
@ToString
@AllArgsConstructor
@Slf4j
public class AccountDto {
    private String accountNumber; // 계좌번호
    private String nickname; // 계좌명
    private LocalDateTime registeredAt; // 계좌등록일시

    //자바빈규약 : 필드에 직접접근불가 (properties는 private으로 두어 getter, setter이용해 접근할 것.)

    /* 1. getter, setter (@Getter, @Setter)
        getter, setter를 일일이 생성할 경우
        1. 필드 추가 시 해당 getter, setter를 추가해야하는 번거로움 존재.
        2. 변수명 변경시 해당 메서드들의 이름도 모두 변경해야하는 번거로움 존재.
        3. 코드가 길어짐.
    */
    /* 2. 생성자 어노테이션
        @NoArgsConstructor // 매개변수가 없는 생성자
        @AllArgsConstructor // 모든 필드를 매개변수로 가지는 생성자
        @RequiredArgsConstructor // 필수값만 매개변수로 가지는 생성자
    */
    /* 3. 로그출력 (@Slf4j)
         log 들은 각 클래스의 정보를 담고 있어야 한다.
         따라서 각 클래스마다 매번 상단에 아래의 코드를 반복적으로 작성해준 후 Logger를 사용한 log 출력이 가능하다.
         private static final Logger log = org.slf4j.LoggerFactory.getLogger(AccountDto.class);
         log.error("error is occurred");
     */
    /* 4. @ToString
        기존 Object의 toString()은 객체의 주소정보값을 출력한다.
        이 toString() 메서드를 자동 오버라이드해서 객체의 데이터를 보여준다.
    */
    /* 5. @Data : 보안상의 문제, equals 자동생성등의 문제 지님.
        해당 어노테이션은 @Getter, @Setter, @ToString 등을 포함한다.
        따라서 개인정보 등 중요 정보를 담는 필드가 존재하는 경우
        toString() 메서드 호출 시 개인정보 유출의 위험이 있기 때문에
        자동 오버라이드되어 출력되지 않도록 해야한다.
    */
}
