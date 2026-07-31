package com.lemonade0209.minibank.account.repository;

import com.lemonade0209.minibank.account.domain.Account;

import java.util.List;

public interface AccountRepository {

    Account save(Account account);

    Account findById(Long id);

    List<Account> findAll();
}
