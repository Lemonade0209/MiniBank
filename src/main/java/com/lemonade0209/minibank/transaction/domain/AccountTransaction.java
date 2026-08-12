package com.lemonade0209.minibank.transaction.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AccountTransaction {
    private Long id;
    private final Long accountId;
    private final TransactionType type;
    private final long amount;
    private final long balanceAfter;
    private final Long counterpartyAccountId;
    private final String transferGroupId;
    private final LocalDateTime createdAt;

    public AccountTransaction(Long accountId,
                              TransactionType type,
                              long amount,
                              long balanceAfter,
                              Long counterpartyAccountId,
                              String transferGroupId) {
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.counterpartyAccountId = counterpartyAccountId;
        this.transferGroupId = transferGroupId;
        this.createdAt = LocalDateTime.now();
    }

    public void setId(Long id) {
        this.id = id;
    }
}
