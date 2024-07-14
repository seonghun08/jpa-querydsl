package jpabook.jpashop;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.QMember;
import jpabook.jpashop.domain.QTeam;
import jpabook.jpashop.domain.Team;
import jpabook.jpashop.repository.TeamRepository;
import jpabook.jpashop.repository.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static jpabook.jpashop.domain.QMember.member;
import static jpabook.jpashop.domain.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;

    private JPAQueryFactory query;

    private void close() {
        em.flush(); // 강제로 insert 쿼리를 날림
        em.clear(); // 영속성 컨텍스트 초기화
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
                .where(member.name.eq("member5"))
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
     * 단, 2에서 회원 이름이 존재하지 않으면 마지막에 출력(nulls last)
     */
    @Test
    void sort() {
        memberRepository.save(new Member(null, 100));
        memberRepository.save(new Member("spring", 100));
        memberRepository.save(new Member("querydsl", 100));

        List<Member> members = query
                .selectFrom(member)
                .where(member.age.eq(100))
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

    @Test
    void aggregation() {
        List<Tuple> result = query // querydsl tuple
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(5);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(20);
        assertThat(tuple.get(member.age.max())).isEqualTo(30);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    @Test
    void groupBy() {
        List<Tuple> result = query
                .select(
                        team.name,
                        member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .having(member.age.avg().gt(20)) // 회원 평균 나이 20세 이상만 조회
                .fetch();

        Tuple teamB = result.get(0);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(25);
    }

    @Test
    void join() {
        List<Member> members = query
                .selectFrom(member)
                .join(member.team, team).on(team.name.eq("teamA"))
                .where(team.name.eq("teamA"))
                .fetch();

        // members.forEach(System.out::println);
        assertThat(members.size()).isEqualTo(2);
        assertThat(members)
                .extracting("name")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     * - but 외부 조인 불가능
     */
    @Test
    void thetaJoin() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> members = query
                .select(member)
                .from(member, team)
                .where(member.name.eq(team.name))
                .fetch();

        members.forEach(System.out::println);
    }

    /**
     * ex) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     */
    @Test
    void joinOnFiltering() {
        List<Tuple> result = query
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        result.forEach(System.out::println);
    }

    @Test
    void joinOnNoRelation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = query
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.name.eq(team.name)) // team -> id 매칭 X, member.name, team.name 만 매칭
                // .leftJoin(member.team, team).on(member.name.eq(team.name)) // member.team, team -> id 매칭
                .fetch();

        result.forEach(System.out::println);
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    void fetchJoinNo() {
        close();

        Member findMember = query
                .selectFrom(member)
                .where(member.name.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }

    @Test
    void fetchJoinUse() {
        close();

        Member findMember = query
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.name.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치  조인 적용").isTrue();
    }

    @Test
    void subQuery() {
        QMember memberSub = new QMember("memberSub");

        // 나이가 가장 많은 회원 조회
        List<Member> maxMembers = query
                .selectFrom(member)
                .where(member.age.eq(
                        // 서브쿼리
                        select(memberSub.age.max())
                                .from(memberSub)))
                .fetch();

        maxMembers.forEach(System.out::println);

        // 나이가 평균 이상인 회원
        List<Member> avgMembers = query
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)))
                .fetch();

        avgMembers.forEach(System.out::println);
    }

    @Test
    void subQueryIn() {
        QMember memberSub = new QMember("memberSub");

        List<Member> members = query
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(member.age.gt(15))))
                .fetch();

        members.forEach(System.out::println);
    }

    @Test
    void selectSubQuery() {
        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = query
                .select(
                        member.name,
                        select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();

        result.forEach(System.out::println);
    }

    @Test
    void basicCase() {
        List<Tuple> results = query
                .select(member.name,
                        member.age
                                .when(10).then("10살")
                                .when(20).then("20살")
                                .otherwise("기타"))
                .from(member)
                .fetch();

        results.forEach(System.out::println);
    }

    @Test
    void complexCase() {

        List<Tuple> results = query
                .select(member.name,
                        new CaseBuilder()
                                .when(member.age.between(0, 10)).then("0 ~ 10살")
                                .when(member.age.between(11, 20)).then("11 ~ 20살")
                                .when(member.age.between(21, 30)).then("21 ~ 30살")
                                .otherwise("기타"))
                .from(member)
                .fetch();

        results.forEach(System.out::println);
    }

    @Test
    void constant() {
        List<Tuple> result = query
                .select(member.name, Expressions.constant("A"))
                .from(member)
                .fetch();

        result.forEach(System.out::println);
    }

    @Test
    void concat() {
        StringExpression memberAge = member.age.stringValue();

        List<String> results = query
                .select(member.name.concat(" : ").concat(memberAge))
                .from(member)
                .fetch();

        results.forEach(System.out::println);
    }

    /**
     * Repository 계층까진 Tuple 을 그대로 사용해서 무난하나,
     * 앞단까지 사용하는 것을 지양하고 DTO 사용 권장
     */
    @Test
    void tupleProjection() {
        List<Tuple> result = query
                .select(member.name, member.age)
                .from(member)
                .fetch();

        result.forEach(tuple -> {
            String name = tuple.get(member.name);
            Integer age = tuple.get(member.age);
            System.out.println("name = " + name);
            System.out.println("age = " + age);
        });
    }


}
