package jpabook.jpashop.domain.member;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Team;
import jpabook.jpashop.repository.MemberDto;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

@Transactional
@SpringBootTest
class MemberRepositoryTest {

    @Autowired EntityManager entityManager;
    @Autowired TeamRepository teamRepository;
    @Autowired MemberRepository memberRepository;

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

    private void close() {
        entityManager.flush();
        entityManager.clear();
    }
}
