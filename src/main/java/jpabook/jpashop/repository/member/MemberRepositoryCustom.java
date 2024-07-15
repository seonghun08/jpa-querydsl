package jpabook.jpashop.repository.member;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.member.dto.MemberSearchCondition;
import jpabook.jpashop.repository.member.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {

    List<Member> findMemberCustom();

    List<MemberTeamDto> search(MemberSearchCondition condition);

    Page<MemberTeamDto> searchSimple(MemberSearchCondition condition, Pageable pageable);

    Page<MemberTeamDto> searchComplex(MemberSearchCondition condition, Pageable pageable);
}
