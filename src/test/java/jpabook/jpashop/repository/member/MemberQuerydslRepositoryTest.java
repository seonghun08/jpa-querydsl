package jpabook.jpashop.repository.member;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Team;
import jpabook.jpashop.repository.TeamRepository;
import jpabook.jpashop.repository.member.dto.MemberSearchCondition;
import jpabook.jpashop.repository.member.dto.MemberTeamDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberQuerydslRepositoryTest {

    @Autowired EntityManager em;
    @Autowired MemberQuerydslRepository memberQuerydslRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;

    private JPAQueryFactory query;

    private void reset() {
        em.flush();
        em.clear();
    }

    @BeforeEach
    void before() {
        query = new JPAQueryFactory(em);

        Team teamA = teamRepository.save(new Team("teamA"));
        Team teamB = teamRepository.save(new Team("teamB"));

        memberRepository.save(new Member("member1", 10, teamA));
        memberRepository.save(new Member("member2", 15, teamA));
        memberRepository.save(new Member("member3", 20, teamB));
        memberRepository.save(new Member("member4", 25, teamB));
        memberRepository.save(new Member("member5", 30, teamB));

        reset();
    }

    @Test
    void searchByBuilder() {
        MemberSearchCondition cond = new MemberSearchCondition();
        cond.setAgeGoe(15);
        cond.setAgeLoe(25);

        List<MemberTeamDto> result = memberQuerydslRepository.searchByBuilder(cond);
        // result.forEach(System.out::println);

        assertThat(result)
                .extracting("name")
                .contains("member2", "member3", "member4");
    }

    @Test
    void searchByWhere() {
        MemberSearchCondition cond = new MemberSearchCondition();
        cond.setAgeGoe(15);
        cond.setAgeLoe(25);

        List<MemberTeamDto> result = memberQuerydslRepository.search(cond);
        // result.forEach(System.out::println);

        assertThat(result)
                .extracting("name")
                .contains("member2", "member3", "member4");
    }

    @Test
    void searchMember() {
        MemberSearchCondition cond = new MemberSearchCondition();
        cond.setAgeGoe(15);
        cond.setAgeLoe(25);

        List<Member> result = memberQuerydslRepository.searchMember(cond);
        // result.forEach(System.out::println);

        assertThat(result)
                .extracting("name")
                .contains("member2", "member3", "member4");
    }
}