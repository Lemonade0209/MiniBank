package com.lemonade0209.minibank.member.domain;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class Member {
    private Long id;
    private String loginId;
    private String password;
    private String name;
    private final LocalDateTime createdAt;

    public Member() {
        this.createdAt = LocalDateTime.now();
    }

    public Member(String loginId, String password, String name) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }
}
