package jpabook.jpashop;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static jpabook.jpashop.domain.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;
    @Autowired
    MemberRepository memberRepository;

    private JPAQueryFactory query;

    @BeforeEach
    void before() {
        query = new JPAQueryFactory(em);
        memberRepository.save(new Member("member1", 10));
        close();
    }

    @Test
    void startJPQL() {
        // username: member1 조회
        final String name = "member1";
        String query = "select m from Member m where name = :name";

        Member findMember = em.createQuery(
                        query, Member.class)
                .setParameter("name", name)
                .getSingleResult();

        assertThat(findMember).isNotNull();
        assertThat(findMember.getName()).isEqualTo(name);
    }

    @Test
    void startQuerydsl() {
        memberRepository.save(new Member("member1"));
        close();

        // username: member1 조회
        final String name = "member1";

        Member findMember = query
                .selectFrom(member)
                .where(member.name.eq(name))
                .fetchOne();

        assertThat(findMember).isNotNull();
        assertThat(findMember.getName()).isEqualTo(name);
    }

    @Test
    void search() {
        final String name = "member1";
        Member findMember = query
                .selectFrom(member)
                .where(member.name.eq(name)
                        .and(member.age.between(10, 30)))
                .fetchOne();

        assertThat(findMember).isNotNull();
        assertThat(findMember.getName()).isEqualTo(name);
    }

    @Test
    void searchAndParam() {
        final String name = "member1";
        Member findMember = query
                .selectFrom(member)
                .where(
                        member.name.eq(name),
                        member.age.between(10, 30))
                .fetchOne();

        assertThat(findMember).isNotNull();
        assertThat(findMember.getName()).isEqualTo(name);
    }

    @Test
    void resultFetch() {

        // List
        List<Member> fetch = query
                .selectFrom(member)
                .fetch();
        // 단 건
        Member findMember1 = query
                .selectFrom(member)
                .fetchOne();

        // 처음 한 건 조회
        Member findMember2 = query
                .selectFrom(member)
                .fetchFirst();

        // 페이징에서 사용 -> deprecated
        QueryResults<Member> results = query
                .selectFrom(member)
                .fetchResults();

        long total = results.getTotal();
        List<Member> results1 = results.getResults();

        // count -> deprecated
        long totalCount = query
                .selectFrom(member)
                .fetchCount();
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     *     단, 2에서 회원 이름이 존재하지 않으면 마지막에 출력(nulls last)
     */
    @Test
    void sort() {
        memberRepository.save(new Member(null, 100));
        memberRepository.save(new Member("spring", 100));
        memberRepository.save(new Member("querydsl", 100));

        List<Member> members = query
                .selectFrom(member)
                .where(
                       member.age.eq(100)
                )
                .orderBy(
                        member.age.desc(),
                        member.name.asc().nullsLast())
                .fetch();

        // 정렬이 되었다면 querydsl -> spring -> null 순 이어야 한다.
        assertThat(members.get(0).getName()).isEqualTo("querydsl");
        assertThat(members.get(1).getName()).isEqualTo("spring");
        assertThat(members.get(2).getName()).isNull();

        assertThat(members.size()).isEqualTo(3);
    }

    private void close() {
        em.flush(); // 강제로 insert 쿼리를 날림
        em.clear(); // 영속성 컨텍스트 초기화
    }
}
