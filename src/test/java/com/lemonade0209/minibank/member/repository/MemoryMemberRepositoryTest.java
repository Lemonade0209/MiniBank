package com.lemonade0209.minibank.member.repository;

import com.lemonade0209.minibank.member.domain.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class MemoryMemberRepositoryTest {

    private MemberRepository memberRepository;
    @BeforeEach
    void BeforeEach(){
        memberRepository = new MemoryMemberRepository();
    }

    @Test
    void save(){
        //given
        Member member = new Member("member1","pw0001","홍길동");
        //when
        Member savedMember = memberRepository.save(member);
        //then
        assertThat(savedMember).isEqualTo(member);

    }
    @Test
    void findById(){
        //given
        Member member = new Member("member1","pw0001","홍길동");
        //when
        Member savedMember = memberRepository.save(member);
        //then
        assertThat(savedMember.getId()).isNotNull();
        assertThat(memberRepository.findById(savedMember.getId())).isEqualTo(member);
    }
    @Test
    void findByLoginId(){
        //given
        Member member = new Member("member1","pw0001","홍길동");
        //when
        Member savedMember = memberRepository.save(member);
        //then
        assertThat(memberRepository.findByLoginId("member1")).isEqualTo(member);
    }

}