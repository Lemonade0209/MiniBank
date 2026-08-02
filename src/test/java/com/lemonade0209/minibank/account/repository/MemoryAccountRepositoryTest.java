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

    @Test
    void findByAccountNumber() {
        // given
        Account account = new Account(1L, "100000000001");
        accountRepository.save(account);

        // when
        Account foundAccount = accountRepository.findByAccountNumber("100000000001");

        // then
        assertThat(foundAccount).isSameAs(account);
    }

    @Test
    void findByMemberId() {
        // given
        Account member1Account1 = new Account(1L, "100000000001");
        Account member1Account2 = new Account(1L, "100000000002");
        Account member2Account = new Account(2L, "100000000003");
        accountRepository.save(member1Account1);
        accountRepository.save(member1Account2);
        accountRepository.save(member2Account);

        // when
        var accounts = accountRepository.findByMemberId(1L);

        // then
        assertThat(accounts).containsExactlyInAnyOrder(member1Account1, member1Account2);
    }
}
