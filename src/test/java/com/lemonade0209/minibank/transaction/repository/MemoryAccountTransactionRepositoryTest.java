package com.lemonade0209.minibank.transaction.repository;

import com.lemonade0209.minibank.transaction.domain.AccountTransaction;
import com.lemonade0209.minibank.transaction.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.lemonade0209.minibank.transaction.domain.TransactionType.DEPOSIT;
import static com.lemonade0209.minibank.transaction.domain.TransactionType.WITHDRAWAL;
import static org.assertj.core.api.Assertions.assertThat;

class MemoryAccountTransactionRepositoryTest {

    private MemoryAccountTransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MemoryAccountTransactionRepository();
    }

    @Test
    void save() {
        // given
        AccountTransaction transaction = transaction(1L, DEPOSIT, 10_000L, 10_000L);

        // when
        AccountTransaction savedTransaction = repository.save(transaction);

        // then
        assertThat(savedTransaction.getId()).isNotNull();
        assertThat(repository.findByAccountId(1L)).containsExactly(transaction);
    }

    @Test
    void findByAccountIdInLatestOrder() {
        // given
        AccountTransaction first = transaction(1L, DEPOSIT, 10_000L, 10_000L);
        AccountTransaction otherAccount = transaction(2L, DEPOSIT, 5_000L, 5_000L);
        AccountTransaction second = transaction(1L, WITHDRAWAL, 4_000L, 6_000L);
        repository.save(first);
        repository.save(otherAccount);
        repository.save(second);

        // when
        var transactions = repository.findByAccountId(1L);

        // then
        assertThat(transactions).containsExactly(second, first);
    }

    private AccountTransaction transaction(Long accountId,
                                           TransactionType type,
                                           long amount,
                                           long balanceAfter) {
        return new AccountTransaction(
                accountId,
                type,
                amount,
                balanceAfter,
                null,
                null
        );
    }
}
