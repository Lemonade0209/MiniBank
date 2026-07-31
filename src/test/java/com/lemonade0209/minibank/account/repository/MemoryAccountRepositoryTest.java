package com.lemonade0209.minibank.account.repository;

import com.lemonade0209.minibank.account.domain.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryAccountRepositoryTest {

    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        accountRepository = new MemoryAccountRepository();
    }

    @Test
    void save() {
        // given
        Account account = new Account(1L, "100000000001");

        // when
        Account savedAccount = accountRepository.save(account);

        // then
        assertThat(savedAccount).isSameAs(account);
        assertThat(savedAccount.getId()).isNotNull();
    }

    @Test
    void findById() {
        // given
        Account account = new Account(1L, "100000000001");
        Account savedAccount = accountRepository.save(account);

        // when
        Account foundAccount = accountRepository.findById(savedAccount.getId());

        // then
        assertThat(foundAccount).isSameAs(savedAccount);
    }

    @Test
    void findAll() {
        // given
        Account account1 = new Account(1L, "100000000001");
        Account account2 = new Account(2L, "100000000002");
        accountRepository.save(account1);
        accountRepository.save(account2);

        // when
        var accounts = accountRepository.findAll();

        // then
        assertThat(accounts).containsExactlyInAnyOrder(account1, account2);
    }
}
