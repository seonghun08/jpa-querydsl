package jpabook.jpashop.domain;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.repository.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static jpabook.jpashop.domain.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberTest {

    @Autowired
    EntityManager em;
    @Autowired
    MemberRepository memberRepository;

    @Test
    void testEntity() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        close();

        List<Member> members = em.createQuery(
                        "select m from Member m join fetch m.team t", Member.class)
                .getResultList();

        members.forEach(m -> {
            System.out.println("members = " + m);
            System.out.println("-> member.team = " + m.getTeam());
        });
    }

    @Test
    void JpaEventBaseEntity() throws Exception {
        // given
        Member member = memberRepository.save(new Member("member1"));// @PrePersist

        Thread.sleep(1000);
        member.setName("member2");

        close(); // @PreUpdate

        // when
        Member findMember = memberRepository.findById(member.getId()).get();

        // given
        System.out.println("findMember.getCreatedDate() = " + findMember.getCreatedDate());
        System.out.println("findMember.getLastModifiedDate() = " + findMember.getLastModifiedDate());
        System.out.println("findMember.getCreatedBy() = " + findMember.getCreatedBy());
        System.out.println("findMember.getLastModifiedBy() = " + findMember.getLastModifiedBy());
    }

    private void close() {
        em.flush(); // 강제로 insert 쿼리를 날림
        em.clear(); // 영속성 컨텍스트 초기화
    }
}