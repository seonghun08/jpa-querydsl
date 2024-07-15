package jpabook.jpashop;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.QMember;
import jpabook.jpashop.domain.Team;
import jpabook.jpashop.repository.TeamRepository;
import jpabook.jpashop.repository.member.dto.MemberDto;
import jpabook.jpashop.repository.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static jpabook.jpashop.domain.QMember.member;
import static jpabook.jpashop.domain.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslDynamicQueryTest {

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
    void dynamicQueryBooleanBuilder() {
        final String nameParam = "member";
        final Integer ageParam = null;

        // final String nameParam = "member1";
        // final Integer ageParam = 10;

        List<Member> members = searchBooleanBuilderByMember(nameParam, ageParam);

        // members.forEach(System.out::println);
        assertThat(members.size()).isEqualTo(5);
    }

    private List<Member> searchBooleanBuilderByMember(String nameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();

        if (nameCond != null) {
            builder.and(member.name.like("%" + nameCond + "%"));
        }

        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }

        return query
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    void dynamicQueryWhereParam() {
        final String nameParam = "member";
        final Integer ageParam = 10;

        List<Member> members = searchWhereParamByMember(nameParam, ageParam);
        List<MemberDto> memberDtos = searchWhereParamByMemberDto(nameParam, ageParam);

        // members.forEach(System.out::println);
        memberDtos.forEach(System.out::println);

        // assertThat(members.size()).isEqualTo(1);
    }

    private List<Member> searchWhereParamByMember(String nameCond, Integer ageCond) {
        return query
                .selectFrom(member)
                // .where(usernameLike(nameCond), ageEq(ageCond))
                .where(allEq(nameCond, ageCond))
                .fetch();
    }

    private List<MemberDto> searchWhereParamByMemberDto(String nameCond, Integer ageCond) {
        return query
                .select(Projections.constructor(MemberDto.class,
                        member.id,
                        member.name,
                        member.age,
                        team.name))
                .from(member)
                .join(member.team, team)
                .where(allEq(nameCond, ageCond))
                .fetch();
    }

    private BooleanExpression allEq(String nameCond, Integer ageCond) {
        return usernameLike(nameCond).and(ageEq(ageCond));
    }

    private BooleanExpression usernameLike(String nameCond) {
        return nameCond == null ? null : member.name.like("%" + nameCond + "%");
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond == null ? null : member.age.eq(ageCond);
    }
}
