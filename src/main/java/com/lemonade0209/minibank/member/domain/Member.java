package com.lemonade0209.minibank.member.domain;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class Member {
    private Long id;
    private final String loginId;
    private final String password;
    private final String name;
    private final LocalDateTime createdAt;

    public Member(String loginId, String password, String name){
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    public void setId(Long id){
        this.id = id;
    }

}
