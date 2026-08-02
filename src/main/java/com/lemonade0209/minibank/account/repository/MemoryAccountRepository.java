package com.lemonade0209.minibank.account.repository;

import com.lemonade0209.minibank.account.domain.Account;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MemoryAccountRepository implements AccountRepository {

    private final Map<Long, Account> store = new HashMap<>();
    private long sequence = 0L;

    @Override
    public Account save(Account account) {
        account.setId(++sequence);
        store.put(account.getId(), account);
        return account;
    }

    @Override
    public Account findById(Long id) {
        return store.get(id);
    }

    @Override
    public List<Account> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Account findByAccountNumber(String accountNumber) {
        return store.values().stream()
                .filter(account -> account.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Account> findByMemberId(Long memberId) {
        return store.values().stream()
                .filter(account -> account.getMemberId().equals(memberId))
                .toList();
    }
}
