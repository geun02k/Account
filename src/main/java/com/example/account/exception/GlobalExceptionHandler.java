package com.example.account.exception;

import com.example.account.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.example.account.type.ErrorCode.INTERVAL_SERVER_ERROR;
import static com.example.account.type.ErrorCode.INVALID_REQUEST;

// 일관성있는 예외처리
// 1. 문제점
// 현재 에러메시지는 아래의 json 데이터와 같이 출력된다.
// 따라서 어떤 사유로 에러가 발생하는지 파악하기 어렵다.
// {
//  "timestamp": "2024-12-05T08:15:55.971+00:00",
//  "status": 500,
//  "error": "Internal Server Error",
//  "path": "/transaction/cancel"
// }
// 실제 정의해놓은 에러는 ErrorCode.java에 정의해놓은 코드임에도 불구하고
// 정확한 에러 원인을 클라이언트에서 확인하기 어려운 구조이다.
//
// 2. 해결방법 (여기서는 세번쨰 방법 사용)
// - Http status code 사용 ex) 200 OK, 404 NOT FOUND
// - 별도의 status code 사용 ex) 사용자정의상태코드 사용 0000(성공) -1001(어떤실패1), -1002(어떤실패2)
// - errorCode(문자코드)와 errorMessage를 사용
// {
//  "errorCode": "USER_NOT_FOUND"
//  "errorMessage": "사용자가 없습니다."
// }

// 현재는 어플리케이션에서 exception을 처리하지 않기 때문에 예외가 발생하면
// controller에서 try를 하고있지 않기 때문에
// 그냥 예외발생한 부분을 외부에 응답으로 보내버린다.
// controller마다 try-catch를 일일이 해줄수도 있지만
// 코드도 보기가 어렵고 유질보수는 더 어려워진다.
// 따라서 global한 exception을 만들어 주어 처리하는 것이 좋다.

@Slf4j
@RestControllerAdvice // 모든 controller에서 발생한 exception들을 처리
public class GlobalExceptionHandler {

    // 1. 사용자정의 예외
    // AccountException이 발생했을 때 해당 예외의 errorCode, errorMessage를
    // ErrorResponse 객체에 담아 응답으로 전달한다.
    @ExceptionHandler(AccountException.class)
    public ErrorResponse handleAccountException(AccountException e) {
        log.error("{} is occurred.", e.getErrorCode());

        // 인자 수가 적을때는 굳이 builder를 사용하지 않아도된다.
        return new ErrorResponse(e.getErrorCode(), e.getErrorMessage());
    }

    // 2. 일반적 예외
    // 일반적으로 자바나 스프링에서 정의된 예외들 중 자주 발생하는 것들도 등록해둔다.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleMethodArgumentNotValidExceptionException(
            MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException is occurred.", e);

        return new ErrorResponse(INVALID_REQUEST, INVALID_REQUEST.getDescription());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ErrorResponse handleDataIntegrityViolationException(
            DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException is occurred.", e);

        return new ErrorResponse(INVALID_REQUEST, INVALID_REQUEST.getDescription());
    }

    // 3. 모든 예외를 아우르는 최종 예외
    // AccountException 외 모든 예외에 대한 처리 수행. (필수)
    // 서버에서 예상치 못한 예외(우리가 정의해주지않은 예외)가 발생한 것이므로
    // INTERVAL_SERVER_ERROR(서버내부오류)를 내려준다.
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleException(Exception e) {
        log.error("Exception is occurred.", e);

        return new ErrorResponse(INTERVAL_SERVER_ERROR,
                INTERVAL_SERVER_ERROR.getDescription());
    }


}
