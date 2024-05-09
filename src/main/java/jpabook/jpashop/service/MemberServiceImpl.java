package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Override
    public Long signUp(Member member) {
        validateDuplicateMember(member);
        return memberRepository.save(member).getId();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    private void validateDuplicateMember(Member member) {
        if (memberRepository.existsByName(member.getName())) {
            throw new IllegalArgumentException("중복된 이름이 존재합니다.");
        }
    }
}
