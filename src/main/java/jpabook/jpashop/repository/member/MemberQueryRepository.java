package jpabook.jpashop.repository.member;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

    private final EntityManager em;

    public List<Member> findAll() {
        return em.createQuery(
                        "select m from Member m", Member.class)
                .getResultList();
    }
}
