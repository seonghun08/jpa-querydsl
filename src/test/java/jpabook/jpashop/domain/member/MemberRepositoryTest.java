package jpabook.jpashop.domain.member;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Team;
import jpabook.jpashop.repository.MemberDto;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Commit;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    EntityManager entityManager;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    void beforeEach() {
        memberRepository.deleteAll();
    }

    @Test
    @Commit
    void findByName() {
        // given
        final String name = "spring";
        final String city = "city";
        final String street = "street";
        final String zipcode = "zipcode";

        Address address = new Address(city, street, zipcode);
        Member member = Member.builder()
                .name(name)
                .address(address)
                .build();

        memberRepository.save(member);
        close();

        // when
        Member findMember = memberRepository.findByName(name)
                .orElse(null);

        // then
        assertThat(findMember).isNotNull();
        assertThat(findMember.getName()).isEqualTo(name);
        assertThat(findMember.getAddress().getCity()).isEqualTo(city);
        assertThat(findMember.getAddress().getStreet()).isEqualTo(street);
        assertThat(findMember.getAddress().getZipcode()).isEqualTo(zipcode);
    }

    @Test
    void findUsernameList() {
        Member m1 = new Member("AAA");
        Member m2 = new Member("BBB");

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> list = memberRepository.findUsernameList();
        list.forEach(s -> System.out.println("s = " + s));
    }

    @Test
    void findMemberDto() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member memberA = new Member("AAA");
        Member memberB = new Member("BBB");
        memberA.changeTeam(team);
        memberB.changeTeam(team);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        memberDto.forEach(System.out::println);
    }

    @Test
    void findByAges() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 15);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> members = memberRepository.findByAges(Arrays.asList(10, 15));
        members.forEach(System.out::println);
    }

    @Test
    void returnType() {
        final String name = "AAA";
        Member member = new Member(name);
        memberRepository.save(member);

        List<Member> listByName = memberRepository.findListByName(name); // 값이 비워져 있어도 빈 컬렉션을 반환해준다. (즉, null 이 아님)
        Member memberByName = memberRepository.findMemberByName(name); // 값이 비워져 있다면 null 반환
        Optional<Member> optionalByName = memberRepository.findOptionalByName(name);

        assertThat(listByName.get(0))
                .isEqualTo(memberByName)
                .isEqualTo(optionalByName.orElse(null));
    }

    @Test
    void paging() {
        // given
        for (int i = 1; i < 10; i++) {
            memberRepository.save(new Member("member" + i, 10));
        }

        final int age = 10;
        final PageRequest pageRequest = PageRequest.of(
                0, 3, Sort.by(Sort.Direction.DESC, "name"));

        // when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);
        Slice<Member> slice = memberRepository.findSliceByAge(age, pageRequest);

        Page<MemberDto> memberDtoPage = page.map(m -> new MemberDto(m.getId(), m.getName(), null));

        // then
        List<Member> members = page.getContent();
        long totalElements = page.getTotalElements();

        members.forEach(System.out::println);
        System.out.println("totalElements = " + totalElements);
        memberDtoPage.getContent().forEach(System.out::println);

        // page
        assertThat(members.size()).isEqualTo(3);
        assertThat(totalElements).isEqualTo(9);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();

        // slice
        assertThat(slice.getContent().size()).isEqualTo(3);
        assertThat(slice.getNumber()).isEqualTo(0);
        assertThat(slice.isFirst()).isTrue();
        assertThat(slice.hasNext()).isTrue();
    }

    /**
     * bulk 연산 쿼리 주의점!!
     * - JPA 는 영속성 컨텍스트를 통해 트랜잭션이 끝나는 시점에 쿼리를 한번에 보낸다.
     * 이로 인해 같은 트랜잭션 내 조회 쿼리를 사용할 경우, 업데이트 쿼리가 아직 발생되지 않아 변경되지 않은 값이 조회된다.
     * - 해당 문제로 인해 bulk 연산을 한 시점에 바로 flush() -> clear() 를 진행 하는 것이 안전하다.
     */
    @Test
    void bulkUpdate() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 25));
        memberRepository.save(new Member("member3", 30));
        memberRepository.save(new Member("member4", 11));
        memberRepository.save(new Member("member5", 20));

        // when
        int resultCount = memberRepository.bulkAgePlus(20);

        /*
         * @Modifying(clearAutomatically = true)
         * clearAutomatically true 로 설정할 경우, 쿼리가 실행되고 영속성 컨텍스트를 날려준다.
         */
        // close(); // flush -> close

        Member findMember = memberRepository.findByName("member5").get();
        System.out.println("findMember = " + findMember); // close()를 호출하지 않으면 member5는 변경 전으로 조회

        // then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy() {
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        close();

        // when
        List<Member> members = memberRepository.findAll();
        // List<Member> members = memberRepository.findMemberFetchJoin();
        members.forEach(m -> {
            System.out.println("member = " + m.getName());
            System.out.println("member.team = " + m.getTeam().getName());
        });
    }

    @Test
    void queryHint() {
        // given
        Member member = new Member("member1", 10);
        memberRepository.save(member);
        close();

        // when
        // Member findMember = memberRepository.findById(member.getId()).get();
        Member findMember = memberRepository.findReadOnlyByName(member.getName());
        findMember.setName("member2");

        entityManager.flush();
    }

    @Test
    void lock() {
        // given
        Member member = new Member("member1", 10);
        memberRepository.save(member);
        close();

        // when
        List<Member> result = memberRepository.findLockByName(member.getName());
    }

    private void close() {
        entityManager.flush();
        entityManager.clear();
    }
}
