package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service // service타입 빈으로 등록
@RequiredArgsConstructor // 필수 인자(final필드)만 가지는 생성자 자동생성
public class AccountService {
    // @Autowired 같은 어노테이션을 사용 사용하면 의존성을 담아주기가 까다로워진다.
    // so, 요즘에는 필드주입이 권한되지않는다.(생성자주입 권장)
    // 하지만 생성자 주입도 필드명이 변경되거나 새로운 필드가 추가되게되면 생성자를 수정해야하는 등의 이슈 존재.

    // lombok의 @RequiredArgsConstructor : 생성자를 일일이 만들어 사용하지 않고 생성자주입 해결가능.
    // 필드 final 선언 -> 생성자를 통해서만 필드값을 초기화하고 그 외 변경불가. (final은 무조건 생성자에 포함되어있어야 한다.)
    private final AccountRepository accountRepository;

    /** 계좌생성 */
    @Transactional
    public void createAccount() { // Account테이블에 insert
        // Account Entity 생성
        // Account 클래스에 @Builder 어노테이션을 사용했기에 builder() 사용가능.
        // id는 자동생성으로 추가x
        Account account = Account.builder()
                .accountNumber("40000")
                .accountStatus(AccountStatus.IN_USE)
                .build();
        // Account Entity insert
        accountRepository.save(account);
    }
}
