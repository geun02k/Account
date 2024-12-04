package com.example.account.controller;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.dto.AccountDto;
import com.example.account.dto.CreateAccount;
import com.example.account.dto.DeleteAccount;
import com.example.account.service.AccountService;
import com.example.account.service.RedisTestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Autowired
    private ObjectMapper objectMapper; // jackson을 이용해 object <-> 문자열로 변환

    @Test
    void successCreateAccount() throws Exception {
        // given
        // createAccount mocking (createAccount메서드의 실행결과 임의 셋팅)
        given(accountService.createAccount(anyLong(), anyLong()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("1234567890")
                        .registeredAt(LocalDateTime.now())
                        .unRegisteredAt(LocalDateTime.now())
                        .build());
        // when

        // then
        // objectMapper.writeValueAsString(new CreateAccount.Request(1L, 100L)
        // -> 아래 형태의 JSON 문자열 생성, content에 그냥 json형태의 문자열을 삽입해도 결과동일.
        // {
        //  "userId": 1,
        //  "initialBalance": 10000
        // }
        // 그런데 요청값이 실제 응답값에 영향을 주진 않음. (요청 : userId=3333L, accountNumber=1111L)
        // given에서 작성한 결과값을 응답으로 리턴함. (응답 : userId=1, accountNumber=1234567890)
        mockMvc.perform(post("/account") // /account url로 post 전송
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new CreateAccount.Request(3333L, 1111L)))) // controller에 들어갈 요청 (CreateAccount.Request 정보)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andDo(print());

    }

    @Test
    void successDeleteAccount() throws Exception {
        // given
        // deleteAccount mocking
        given(accountService.deleteAccount(anyLong(), anyString()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("1234567890")
                        .registeredAt(LocalDateTime.now())
                        .unRegisteredAt(LocalDateTime.now())
                        .build());
        // when

        // then
        mockMvc.perform(delete("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DeleteAccount.Request(3333L, "1234567890"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andDo(print());

    }

    @Test
    void successGetAccountsByUserId() throws Exception {
        // given
        // 하나의 AccountDto 생성
        // AccountDto.builder()
        //  .accountNumber("1234567890")
        //  .balance(1000L)
        //  .build()
        List<AccountDto> accountDtos =
                Arrays.asList(
                        AccountDto.builder()
                            .accountNumber("1234567890")
                            .balance(1000L)
                            .build(),
                        AccountDto.builder()
                            .accountNumber("1111111111")
                            .balance(2000L)
                            .build()
                );
        given(accountService.getAccountsByUserId(anyLong()))
                .willReturn(accountDtos);

        // when

        // then
        mockMvc.perform(get("/account?user_id=1"))
                .andDo(print())
                .andExpect(jsonPath("$[0].accountNumber").value("1234567890"))
                .andExpect(jsonPath("$[0].balance").value(1000))
                .andExpect(jsonPath("$[1].accountNumber").value("1111111111"))
                .andExpect(jsonPath("$[1].balance").value(2000));
    }

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