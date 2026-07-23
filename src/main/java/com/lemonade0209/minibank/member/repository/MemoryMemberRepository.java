package com.lemonade0209.minibank.member.repository;

import com.lemonade0209.minibank.member.domain.Member;

import java.util.HashMap;
import java.util.Map;

public class MemoryMemberRepository implements MemberRepository {

    private final Map<Long, Member> store = new HashMap<>();
    private long sequence = 0L;

    @Override
    public Member save(Member member) {
        member.setId(++sequence);
        store.put(member.getId(), member);
        return member;
    }

    @Override
    public Member findById(Long id) {
        return store.get(id);
    }

    @Override
    public Member findByLoginId(String loginId){
        return store.values().stream()
                .filter(member-> member.getLoginId().equals(loginId))
                .findFirst().orElse(null);
    }
}
