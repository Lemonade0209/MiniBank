package com.lemonade0209.minibank.member.repository;

import com.lemonade0209.minibank.member.domain.Member;

import java.util.List;

public interface MemberRepository {
    Member save(Member member);

    Member findById(Long id);

    List<Member> findAll();

    Member findByLoginId(String loginId);
}
