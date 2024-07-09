package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberRestController {

    private final MemberRepository memberRepository;

    @GetMapping("/members/{id}")
    public MemberResponse findMember(@PathVariable("id") Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));
        return new MemberResponse(member);
    }

    @GetMapping("/v2/members/{id}")
    public MemberResponse findMember(@PathVariable("id") Member member) {
        return new MemberResponse(member);
    }
}
