package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;

import java.util.List;

public interface MemberService {

    Long signUp(Member member);

    List<Member> findMembers();

    void update(Long id, String name);
}
