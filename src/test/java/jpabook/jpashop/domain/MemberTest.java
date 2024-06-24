package jpabook.jpashop.domain;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberTest {

    @Autowired
    EntityManager entityManager;

    @Test
    void testEntity() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        entityManager.persist(teamA);
        entityManager.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(member3);
        entityManager.persist(member4);

        clear();

        List<Member> members = entityManager.createQuery(
                    "select m from Member m join fetch m.team t", Member.class)
                .getResultList();

        members.forEach(m -> {
            System.out.println("members = " + m);
            System.out.println("-> member.team = " + m.getTeam());
        });
    }

    private void clear() {
        entityManager.flush(); // 강제로 insert 쿼리를 날림
        entityManager.clear(); // 영속성 컨텍스트 초기화
    }
}