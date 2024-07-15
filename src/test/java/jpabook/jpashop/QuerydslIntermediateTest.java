package jpabook.jpashop;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.QMember;
import jpabook.jpashop.domain.Team;
import jpabook.jpashop.repository.TeamRepository;
import jpabook.jpashop.repository.member.dto.MemberDto;
import jpabook.jpashop.repository.member.MemberRepository;
import jpabook.jpashop.repository.member.dto.QMemberDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static jpabook.jpashop.domain.QMember.member;
import static jpabook.jpashop.domain.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslIntermediateTest {

    @Autowired
    EntityManager em;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;

    private JPAQueryFactory query;
    private final static QMember memberSub = new QMember("memberSub");

    private void reset() {
        em.flush();
        em.clear();
    }

    private void printMemberAll() {
        memberRepository.findAll()
                .forEach(System.out::println);
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

    /**
     * Projections
     * DTO 빈 생성(Bean population)
     * - 프로퍼티 접근
     * - 필드 직접 접근
     * - 생성자 사용
     */
    @Test
    void findDto() {
        // getter, setter 주입
        List<MemberDto> memberDto1 = query
                .select(Projections.bean(MemberDto.class,
                        member.id.as("memberId"), // 이름이 다를 때 필요
                        member.name,
                        member.age,
                        team.name.as("teamName"))) // 이름이 다를 때 필요
                .from(member)
                .fetch();

        memberDto1.forEach(System.out::println);

        // field 주입
        List<MemberDto> memberDto2 = query
                .select(Projections.fields(MemberDto.class,
                        member.id.as("memberId"), // 이름이 다를 때 필요
                        member.name,
                        member.age,
                        team.name.as("teamName"))) // 이름이 다를 때 필요
                .from(member)
                .join(member.team, team)
                .fetch();


        memberDto2.forEach(System.out::println);

        // 생성자 주입
        List<MemberDto> memberDto3 = query
                .select(Projections.constructor(MemberDto.class,
                        member.id,
                        member.name,
                        member.age,
                        team.name))
                .from(member)
                .join(member.team, team)
                .fetch();

        memberDto3.forEach(System.out::println);
    }

    @Test
    void findDtoAndSubQuery() {
        // ExpressionUtils.as(source, alias): 필드나, 서브 쿼리 별칭 적용 방법
        // name.as("username"): 필드에 별칭 적용
        List<MemberDto> result = query
                .select(Projections.fields(MemberDto.class,
                        member.name.as("name"),
                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "age")))
                .from(member)
                .fetch();

        result.forEach(System.out::println);
    }

    @Test
    void findDtoByQueryProjection() {
        // @QueryProjection (의존도가 올라가는 문제를 가짐)
        List<MemberDto> result = query
                .select(new QMemberDto(
                        member.id,
                        member.name,
                        member.age,
                        team.name))
                .from(member)
                .join(member.team, team)
                .fetch();

        result.forEach(System.out::println);
    }

    @Test
    void bulkUpdate() {
        // 28살 미만 회원은 이름을 "비회원"으로 변경
        long count = query
                .update(member)
                .set(member.name, "비회원")
                .where(member.age.lt(25))
                .execute();

        reset(); // flush -> clear

        List<Member> members = memberRepository.findAll();
        members.forEach(System.out::println);
    }

    @Test
    void bulkAdd() {
        printMemberAll();

        // 전체 회원의 나이를 1살 더하기
        long count = query
                .update(member)
                .set(member.age, member.age.add(1))
                // .set(member.age, member.age.add(-1)) // -1
                // .set(member.age, member.age.multiply(2)) // x2
                .execute();

        reset();
        printMemberAll();
    }

    @Test
    void bulkDelete() {
        printMemberAll();

        // 회원 중 18살 초과일 경우 삭제
        long count = query
                .delete(member)
                .where(member.age.gt(18))
                .execute();

        reset();
        printMemberAll();
    }

    @Test
    void sqlFucReplace() {
        String fuc = "function('replace', {0}, {1}, {2})";

        // "member" -> "user" 로 replace
        List<String> result = query
                .select(Expressions.stringTemplate(fuc, member.name, "member", "user"))
                .from(member)
                .fetch();

        result.forEach(System.out::println);
    }

    @Test
    void sqlFucLowerCase() {
        String fuc = "function('upper', {0})";

        // {member.name} -> upper
        List<String> result = query
                .select(Expressions.stringTemplate(fuc, member.name))
                .from(member)
                .fetch();

        result.forEach(System.out::println);
    }
}
