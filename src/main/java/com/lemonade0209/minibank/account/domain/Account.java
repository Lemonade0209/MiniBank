package com.lemonade0209.minibank.account.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Account {
    private Long id;
    private Long memberId;
    private String accountNumber;
    private long balance = 0L;
    private final LocalDateTime createdAt;

    public Account(Long memberId, String accountNumber) {
        this.memberId = memberId;
        this.accountNumber = accountNumber;
        this.createdAt = LocalDateTime.now();
    }

    public void setId(Long id) {
        this.id = id;
    }
}
