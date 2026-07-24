package com.lemonade0209.minibank.member.service;

import com.lemonade0209.minibank.member.domain.Member;
import com.lemonade0209.minibank.member.repository.MemberRepository;

public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member join(Member member) {
        if (memberRepository.findByLoginId(member.getLoginId()) != null) {
            throw new IllegalArgumentException("이미 존재하는 로그인 ID입니다.");
        }
        return memberRepository.save(member);
    }
}
