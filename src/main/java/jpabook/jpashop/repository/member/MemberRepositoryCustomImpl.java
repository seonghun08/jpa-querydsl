package jpabook.jpashop.repository.member;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<Member> findMemberCustom() {
        return em.createQuery(
                        "select m from Member m", Member.class)
                .getResultList();
    }
}
