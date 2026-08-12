package com.lemonade0209.minibank.transaction.repository;

import com.lemonade0209.minibank.transaction.domain.AccountTransaction;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;

@Repository
public class MemoryAccountTransactionRepository implements AccountTransactionRepository {

    private final Map<Long, AccountTransaction> store = new LinkedHashMap<>();
    private long sequence = 0L;

    @Override
    public AccountTransaction save(AccountTransaction transaction) {
        transaction.setId(++sequence);
        store.put(transaction.getId(), transaction);
        return transaction;
    }

    @Override
    public List<AccountTransaction> findByAccountId(Long accountId) {
        return store.values().stream()
                .filter(transaction -> transaction.getAccountId().equals(accountId))
                .sorted(comparing(AccountTransaction::getCreatedAt).reversed())
                .toList();
    }
}
