package com.example.account.controller;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.service.AccountService;
import com.example.account.service.RedisTestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest(AccountController.class)
// : value = default로 테스트할 controller 클래스 명시
// : 특정 컨트롤러(AccountController)만 격리시켜 단위테스트수행
@WebMvcTest(AccountController.class)
class AccountControllerTest {
    // AccountController가 의존하는 클래스
    // : AccountService, RedisTestService

    // @MockBean : 빈으로 등록해주는 mock
    //             (자동으로 빈으로 등록되어 AccountController에 주입됨.)
    @MockBean
    private AccountService accountService;

    @MockBean
    private RedisTestService redisTestService;

    @Autowired
    private MockMvc mockMvc;

    // Service쪽 테스트할 때는 when에서 요청한 다음 결과를 받아 확인.
    // controller는 결과가 객체로 만들어지는 게 아닌 http 프로토콜의 응답형식으로 온다.
    // So, 일반적인 방식으로 테스트불가.
    @Test
    void successGetAccount() throws Exception {
        // given
        // given().willReturn() : 테스트에 사용할 Account 객체 생성
        given(accountService.getAccount(anyLong()))
                .willReturn(Account.builder()
                        .accountNumber("3456")
                        .accountStatus(AccountStatus.IN_USE)
                        .build());
        // when

        // then
        // mockMvc.perform() : 모킹된 mockMvc 어플리케이션에 특정 요청 수행.
        mockMvc.perform(get("/account/876")) // get방식으로 /account/876 경로로 요청
                .andDo(print()) // andDo(print()) : 해당요청시 print() 수행 (요청,응답값 console에 표시)
                .andExpect(jsonPath("$.accountNumber").value("3456")) // body의 json 응답 데이터의 accountNumber 값은 3456으로 예상
                .andExpect(jsonPath("$.accountStatus").value("IN_USE"))
                .andExpect(status().isOk()); // 예상되는 결과(결과값은 ok로 예상)
    }
}