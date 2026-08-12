package com.lemonade0209.minibank.transaction.repository;

import com.lemonade0209.minibank.transaction.domain.AccountTransaction;

import java.util.List;

public interface AccountTransactionRepository {

    AccountTransaction save(AccountTransaction transaction);

    List<AccountTransaction> findByAccountId(Long accountId);
}
