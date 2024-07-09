package jpabook.jpashop.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import jpabook.jpashop.domain.Member;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
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

    // Page 는 조회 쿼리와 count 쿼리를 별도 분리하여 최적화할 수 있음!!
    @Query(value = "select m from Member m left join m.team t",
            countQuery = "select count(m.name) from Member m")
    Page<Member> findByAge(int age, Pageable pageable);

    Slice<Member> findSliceByAge(int age, Pageable pageable);

    // bulk query
    // @Modifying // Modifying 가 있어야 executeUpdate 가 실행 (없으면 singleResult() 같은 메서드가 실행)
    @Modifying(clearAutomatically = true) // clearAutomatically true 로 설정할 경우, 쿼리가 실행되고 영속성 컨텍스트를 날려준다.
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    @Query("select m from Member m left join fetch m.team t")
    List<Member> findMemberFetchJoin();

    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    @EntityGraph(attributePaths = {"team"})
    List<Member> findEntityGraphByName(@Param("name") String name);

    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByName(String name);

    @Lock(LockModeType.WRITE)
    List<Member> findLockByName(String name);
}
