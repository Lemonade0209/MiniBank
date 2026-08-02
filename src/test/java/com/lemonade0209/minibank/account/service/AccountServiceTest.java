package com.lemonade0209.minibank.account.service;

import com.lemonade0209.minibank.account.domain.Account;
import com.lemonade0209.minibank.account.repository.MemoryAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountServiceTest {

    private AccountService accountService;
    private MemoryAccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        accountRepository = new MemoryAccountRepository();
        accountService = new AccountService(accountRepository);
    }

    @Test
    void openAccount() {
        // given
        Long memberId = 1L;

        // when
        Account account = accountService.openAccount(memberId);

        // then
        assertThat(account.getId()).isNotNull();
        assertThat(account.getMemberId()).isEqualTo(memberId);
        assertThat(account.getAccountNumber()).isEqualTo("000000000001");
        assertThat(account.getAccountNumber()).hasSize(12);
        assertThat(account.getBalance()).isZero();
        assertThat(accountService.findById(account.getId())).isSameAs(account);
    }

    @Test
    void retryWhenAccountNumberAlreadyExists() {
        // given
        saveAccountsWithNumbers(1, 5);

        // when
        Account account = accountService.openAccount(1L);

        // then
        assertThat(account.getAccountNumber()).isEqualTo("000000000006");
    }

    @Test
    void failWhenAccountNumberRetryIsExhausted() {
        // given
        saveAccountsWithNumbers(1, 6);

        // when & then
        assertThatThrownBy(() -> accountService.openAccount(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("계좌번호 생성에 실패했습니다.");
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

    private void saveAccountsWithNumbers(int start, int end) {
        for (int number = start; number <= end; number++) {
            String accountNumber = String.format("%012d", number);
            accountRepository.save(new Account(2L, accountNumber));
        }
    }
}
