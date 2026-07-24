package com.lemonade0209.minibank.member.service;

import com.lemonade0209.minibank.member.domain.Member;
import com.lemonade0209.minibank.member.repository.MemberRepository;
import com.lemonade0209.minibank.member.repository.MemoryMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberServiceTest {

    private MemberService memberService;
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository = new MemoryMemberRepository();
        memberService = new MemberService(memberRepository);
    }

    @Test
    void join() {
        // given
        Member member = new Member("member1", "pw000001", "홍길동");

        // when
        Member joinedMember = memberService.join(member);

        // then
        assertThat(joinedMember.getId()).isNotNull();
        assertThat(memberRepository.findByLoginId("member1")).isEqualTo(joinedMember);
    }

    @Test
    void duplicateLoginId() {
        // given
        Member member1 = new Member("member1", "pw000001", "홍길동");
        Member member2 = new Member("member1", "pw000002", "김철수");
        memberService.join(member1);

        // when
        assertThatThrownBy(() -> memberService.join(member2));

        // then
        assertThat(member2.getId()).isNull();
        assertThat(memberRepository.findByLoginId("member1")).isEqualTo(member1);
    }
}
