package com.lemonade0209.minibank.account.service;

import com.lemonade0209.minibank.account.domain.Account;
import com.lemonade0209.minibank.account.repository.MemoryAccountRepository;
import com.lemonade0209.minibank.member.domain.Member;
import com.lemonade0209.minibank.member.repository.MemoryMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountServiceTest {

    private AccountService accountService;
    private MemoryAccountRepository accountRepository;
    private MemoryMemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        accountRepository = new MemoryAccountRepository();
        memberRepository = new MemoryMemberRepository();
        accountService = new AccountService(accountRepository, memberRepository);
    }

    @Test
    void openAccount() {
        // given
        Member member = memberRepository.save(
                new Member("member1", "pw000001", "홍길동")
        );

        // when
        Account account = accountService.openAccount(member.getId());

        // then
        assertThat(account.getId()).isNotNull();
        assertThat(account.getMemberId()).isEqualTo(member.getId());
        assertThat(account.getAccountNumber()).isEqualTo("000000000001");
        assertThat(account.getAccountNumber()).hasSize(12);
        assertThat(account.getBalance()).isZero();
        assertThat(accountService.findById(account.getId())).isSameAs(account);
    }

    @Test
    void retryWhenAccountNumberAlreadyExists() {
        // given
        Member member = memberRepository.save(
                new Member("member1", "pw000001", "홍길동")
        );
        saveAccountsWithNumbers(1, 5);

        // when
        Account account = accountService.openAccount(member.getId());

        // then
        assertThat(account.getAccountNumber()).isEqualTo("000000000006");
    }

    @Test
    void failWhenAccountNumberRetryIsExhausted() {
        // given
        Member member = memberRepository.save(
                new Member("member1", "pw000001", "홍길동")
        );
        saveAccountsWithNumbers(1, 6);

        // when & then
        assertThatThrownBy(() -> accountService.openAccount(member.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("계좌번호 생성에 실패했습니다.");
    }

    @Test
    void failWhenMemberDoesNotExist() {
        // when & then
        assertThatThrownBy(() -> accountService.openAccount(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다.");
        assertThat(accountRepository.findAll()).isEmpty();
    }

    @Test
    void findAll() {
        // given
        Account account1 = new Account(1L, "100000000001");
        Account account2 = new Account(2L, "100000000002");
        accountRepository.save(account1);
        accountRepository.save(account2);

        // when
        var accounts = accountService.findAll();

        // then
        assertThat(accounts).containsExactlyInAnyOrder(account1, account2);
    }

    @Test
    void deposit() {
        // given
        Member member = memberRepository.save(
                new Member("member1", "pw000001", "홍길동")
        );
        Account account = accountService.openAccount(member.getId());

        // when
        Account depositedAccount = accountService.deposit(account.getId(), 10_000L);

        // then
        assertThat(depositedAccount.getBalance()).isEqualTo(10_000L);
        assertThat(accountService.findById(account.getId()).getBalance()).isEqualTo(10_000L);
    }

    @Test
    void failWhenDepositAmountIsZero() {
        // given
        Account account = accountRepository.save(new Account(1L, "100000000001"));

        // when & then
        assertThatThrownBy(() -> accountService.deposit(account.getId(), 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("입금액은 0보다 커야 합니다.");
        assertThat(account.getBalance()).isZero();
    }

    @Test
    void failWhenDepositAmountIsNegative() {
        // given
        Account account = accountRepository.save(new Account(1L, "100000000001"));

        // when & then
        assertThatThrownBy(() -> accountService.deposit(account.getId(), -1_000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("입금액은 0보다 커야 합니다.");
        assertThat(account.getBalance()).isZero();
    }

    private void saveAccountsWithNumbers(int start, int end) {
        for (int number = start; number <= end; number++) {
            String accountNumber = String.format("%012d", number);
            accountRepository.save(new Account(2L, accountNumber));
        }
    }
}
