package com.lemonade0209.minibank.member.repository;

import com.lemonade0209.minibank.member.domain.Member;

public interface MemberRepository {
    Member save(Member member);

    Member findById(Long id);

    Member findByLoginId(String loginId);
}
