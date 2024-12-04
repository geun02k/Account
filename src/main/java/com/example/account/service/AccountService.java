package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.account.domain.AccountStatus.IN_USE;
import static com.example.account.type.ErrorCode.*;

@Service // service타입 빈으로 등록
@RequiredArgsConstructor // 필수 인자(final필드)만 가지는 생성자 자동생성
public class AccountService {
    // @Autowired 같은 어노테이션을 사용 사용하면 의존성을 담아주기가 까다로워진다.
    // so, 요즘에는 필드주입이 권한되지않는다.(생성자주입 권장)
    // 하지만 생성자 주입도 필드명이 변경되거나 새로운 필드가 추가되게되면 생성자를 수정해야하는 등의 이슈 존재.

    // lombok의 @RequiredArgsConstructor : 생성자를 일일이 만들어 사용하지 않고 생성자주입 해결가능.
    // 필드 final 선언 -> 생성자를 통해서만 필드값을 초기화하고 그 외 변경불가. (final은 무조건 생성자에 포함되어있어야 한다.)
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    // 반환값
    // Entity 클래스는 다른 클래스와는 다른 성격을 지닌다.
    // 클래스를 레이어간 주고받고 하게되면 Entity에서 레이즈 로딩을 하거나
    // 추가적 쿼리를 날리려고 하면 트랜잭션이 없어 오류 발생 가능.
    // Controller로 쿼리 결과를 전달할 떄
    // Account의 일부 정보만 필요하거나 추가적 데이터가 더 필요할 수 있다.
    // 이 때 Entity는 DB테이블과 1:1 매칭되기 떄문에 변경불가.
    // So, Entity를 반환값으로 넘겨주기 보다는 Controller와 Service간 통신할 때 사용하는 별도의 DTO를 사용한다.
    // 그러면 레이즈 로딩시 발생할 수 트랜잭션 문제를 해결할 수 있고,
    // 전달할 데이터 변화에 대응하기 더 쉬운 구조가 된다.

    /** 계좌생성
     * 1. 사용자 존재여부 확인
     * 2. 계좌번호 생성
     * 3. 계좌 저장 및 정보 전달
     * @param userId 사용자ID
     * @param initialBalance 초기잔액
     * @Return AccountDto 계좌정보
     */
    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {
        // 1. 사용자 존재여부 확인
        // findById()의 리턴값은 optional
        // - 데이터 존재시 accountUser에 저장
        // - optional에서 데이터 미존재시 'User Not Found' 에러발생.
        // 비즈니스의 상황에 맞는 exception이 잘 없는 경우가 많기 때문에 커스텀 exception 사용.
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

        validateCreateAccount(accountUser);

        // 2. 계좌번호 생성 = 마지막계좌번호 + 1 (계좌번호는 총 10자리)
        // - accountRepository.findFirstByOrderByIdDesc() 결과값 존재
        //   -> 문자열 계좌번호를 숫자로 파싱 -> +1 -> 다시 문자열로 변환
        // - accountRepository.findFirstByOrderByIdDesc() 결과값 미존재 = 계좌번호가 하나도 없음
        //   -> 100000000로 계좌번호 생성
        String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
                .map(account -> (Integer.parseInt(account.getAccountNumber())) + 1 + "")
                .orElse("1000000000");

        // 3. 계좌 저장 및 정보 전달
        return AccountDto.fromEntity(
                accountRepository.save(Account.builder()
                        .accountUser(accountUser)
                        .accountStatus(IN_USE)
                        .accountNumber(newAccountNumber)
                        .balance(initialBalance)
                        .registeredAt(LocalDateTime.now())
                        .build())
        );
    }

    private void validateCreateAccount(AccountUser accountUser) {
        // 사용자의 계좌 수 = 최대 10건 이하
        if(accountRepository.countByAccountUser(accountUser) >= 10) {
            throw new AccountException(MAX_ACCOUNT_PER_USER_10);
        }
    }

    /** 계좌해지 */
    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber) {
        // 1. 사용자 존재여부 확인
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
        // 2. 계좌 존재여부 확인
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        validateDeleteAccount(accountUser, account);

        //사용자아이디, 계좌번호, 계좌상태, 해지일시 return
        account.setAccountStatus(AccountStatus.UNREGISTERED);
        account.setUnRegisteredAt(LocalDateTime.now());

        // 아래의 save() 메서드를 사용하지 않아도 동작함.
        // account에 상태값, 해지일시 등 업데이트 되는지 테스트코드에서 확인하기위해 추가.
        // 아래의 방법이 아닌 다른 방법으로도 확인해볼 수 있음.
        // 단, save() 메서드가 deleteAccount()에서 호출되면 혼돈을 줄 수 있음.
        // (불필요한 코드가 추가되더라도 테스트가 원활한 게 더 좋은 경우도 있기 때문.)
        accountRepository.save(account);

        return AccountDto.fromEntity(account);
    }

    private void validateDeleteAccount(AccountUser accountUser, Account account) {
        // 사용자 아이디와 계좌 소유주가 일치여부 확인
        if(!accountUser.getId().equals(account.getAccountUser().getId())) {
            throw new AccountException(USER_ACCOUNT_UN_MATCH);
        }
        // 계좌가 이미 해지 상태인 경우
        if(account.getAccountStatus() == AccountStatus.UNREGISTERED) {
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }
        // 잔액이 있는 경우
        if(account.getBalance() > 0) {
            throw new AccountException(BALANCE_NOT_EMPTY);
        }
    }

    /** 계좌목록조회 */
    @Transactional
    public List<AccountDto> getAccountsByUserId(Long userId) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

        List<Account> accounts = accountRepository.findByAccountUser(accountUser);

        // accounts.stream() : List -> stream으로 변경
        // map(AccountDto::fromEntity) : Account를 AccountDto 타입으로 변경
        //                             = map(account -> AccountDto.fromEntity(account))
        // collect(Collectors.toList()) : stream() -> List 형태로 변경
        return accounts.stream()
                .map(AccountDto::fromEntity)
                .collect(Collectors.toList());
    }

    /** 계좌조회 */
    @Transactional
    public Account getAccount(Long id) {
        if(id < 0) {
            throw new RuntimeException("Minus");
        }

        // findById(id) : id를 통해 select 진행
        // get() : optional로 값을 꺼내는 것은 추천하지 않는 방법이라 경고표시 존재.
        return accountRepository.findById(id).get();
    }
}
