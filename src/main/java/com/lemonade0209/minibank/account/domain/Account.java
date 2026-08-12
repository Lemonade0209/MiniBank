package com.lemonade0209.minibank.account.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Account {
    private Long id;
    private Long memberId;
    private String accountNumber;
    private long balance = 0L;
    private long amount;
    private final LocalDateTime createdAt;

    public Account(Long memberId, String accountNumber) {
        this.memberId = memberId;
        this.accountNumber = accountNumber;
        this.createdAt = LocalDateTime.now();
    }

    public void deposit(long amount) {
        this.balance += amount;
    }

    public void withdraw(long amount) {
        this.balance -= amount;
    }

    public Account() {
        this.createdAt = LocalDateTime.now();
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
