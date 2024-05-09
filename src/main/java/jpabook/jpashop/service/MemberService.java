package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;

import java.util.List;

public interface MemberService {

    Long signUp(Member member);

    List<Member> findMembers();
}
