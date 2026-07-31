package com.lemonade0209.minibank.account.service;

import com.lemonade0209.minibank.account.domain.Account;
import com.lemonade0209.minibank.account.repository.MemoryAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountServiceTest {

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(new MemoryAccountRepository());
    }

    @Test
    void saveAndFindById() {
        // given
        Account account = new Account(1L, "100000000001");

        // when
        Account savedAccount = accountService.save(account);
        Account foundAccount = accountService.findById(savedAccount.getId());

        // then
        assertThat(savedAccount.getId()).isNotNull();
        assertThat(foundAccount).isSameAs(savedAccount);
    }

    @Test
    void findAll() {
        // given
        Account account1 = new Account(1L, "100000000001");
        Account account2 = new Account(2L, "100000000002");
        accountService.save(account1);
        accountService.save(account2);

        // when
        var accounts = accountService.findAll();

        // then
        assertThat(accounts).containsExactlyInAnyOrder(account1, account2);
    }
}
