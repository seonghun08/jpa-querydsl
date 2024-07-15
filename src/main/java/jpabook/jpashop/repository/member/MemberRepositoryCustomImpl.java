package jpabook.jpashop.repository.member;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.member.dto.MemberSearchCondition;
import jpabook.jpashop.repository.member.dto.MemberTeamDto;
import jpabook.jpashop.repository.member.dto.QMemberTeamDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static jpabook.jpashop.domain.QMember.member;
import static jpabook.jpashop.domain.QTeam.team;

@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Member> findMemberCustom() {
        return em.createQuery(
                        "select m from Member m", Member.class)
                .getResultList();
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition cond) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.name,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        nameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageBetween(cond.getAgeGoe(), cond.getAgeLoe())
                )
                .fetch();
    }

    @Override
    public Page<MemberTeamDto> searchSimple(MemberSearchCondition cond, Pageable pageable) {
        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.name,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        nameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageBetween(cond.getAgeGoe(), cond.getAgeLoe())
                )
                .orderBy(member.name.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<MemberTeamDto> searchComplex(MemberSearchCondition cond, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.name,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        nameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageBetween(cond.getAgeGoe(), cond.getAgeLoe())
                )
                .orderBy(member.name.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(Wildcard.count)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        nameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageBetween(cond.getAgeGoe(), cond.getAgeLoe())
                );

        // 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때 또는 마지막 페이지 일 때
        // offset + 컨텐츠 사이즈를 더해서 전체 사이즈 구함, 마지막 페이지이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
        // 쿼리를 생성하지 않음!!
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression nameEq(String name) {
        return StringUtils.hasText(name) ? member.name.eq(name) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageBetween(Integer ageGoe, Integer ageLoe) {
        return combineExpressions(ageGoe(ageGoe), ageLoe(ageLoe));
    }

    private BooleanExpression combineExpressions(BooleanExpression... expressions) {
        BooleanExpression result = Expressions.asBoolean(true).isTrue();

        for (BooleanExpression expression : expressions) {
            if (expression != null) {
                result = result.and(expression);
            }
        }

        return result;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}
