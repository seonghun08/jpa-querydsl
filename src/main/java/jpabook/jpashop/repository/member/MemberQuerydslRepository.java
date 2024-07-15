package jpabook.jpashop.repository.member;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.member.dto.MemberSearchCondition;
import jpabook.jpashop.repository.member.dto.MemberTeamDto;
import jpabook.jpashop.repository.member.dto.QMemberTeamDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static jpabook.jpashop.domain.QMember.member;
import static jpabook.jpashop.domain.QTeam.team;

@Repository
@RequiredArgsConstructor
public class MemberQuerydslRepository {

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    @Transactional
    public void save(Member member) {
        entityManager.persist(member);
    }

    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(queryFactory
                .selectFrom(member)
                .where(member.id.eq(id))
                .fetchOne());
    }

    public List<Member> findAll() {
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    public List<Member> findByName(String name) {
        return queryFactory
                .selectFrom(member)
                .where(member.name.eq(name))
                .fetch();
    }

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition cond) {
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(cond.getUsername())) {
            builder.and(member.name.eq(cond.getUsername()));
        }

        if (StringUtils.hasText(cond.getTeamName())) {
            builder.and(team.name.eq(cond.getTeamName()));
        }

        if (cond.getAgeGoe() != null) {
            builder.and(member.age.goe(cond.getAgeGoe()));
        }

        if (cond.getAgeLoe() != null) {
            builder.and(member.age.loe(cond.getAgeLoe()));
        }

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.name,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();
    }

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
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe())
                )
                .fetch();
    }

    public List<Member> searchMember(MemberSearchCondition cond) {
        return queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(
                        nameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageBetween(cond.getAgeGoe(), cond.getAgeLoe())
                )
                .fetch();
    }

    private BooleanExpression nameEq(String name) {
        return StringUtils.hasText(name) ? member.name.eq(name) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageBetween(Integer ageGoe, Integer ageLoe) {
        return ageGoe(ageGoe).and(ageLoe(ageLoe));
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}
