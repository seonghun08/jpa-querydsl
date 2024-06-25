package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByName(String name);

    boolean existsByName(@Param("name") String name);

    // @NamedQuery (엔티티에 쿼리를 넣어 사용)
    @Query(name = "Member.findByNames")
    List<Member> findByNames(String name);

    // @Query 를 통해 jpql 을 바로 적용 가능
    @Query("select m from Member m where m.name = :name and m.age = :age")
    List<Member> findUser(@Param("name") String name);

    // String 리스트로 반환 
    @Query("select m.name from Member m")
    List<String> findUsernameList();

    // DTO 로 가져오는 방법 (dto 에 join fetch 사용은 안됨)
    @Query("select new jpabook.jpashop.repository.MemberDto(m.id, m.name, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    // IN 절 활용법
    @Query("select m from Member m where m.age in :ages")
    List<Member> findByAges(@Param("ages") List<Integer> age);

    // JpaRepository 는 반환 타입이 바껴도 정상적으로 반환 됨!!
    List<Member> findListByName(String name);
    Member findMemberByName(String name);
    Optional<Member> findOptionalByName(String name);
}
