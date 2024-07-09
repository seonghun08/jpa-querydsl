package jpabook.jpashop.domain.member;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.member.MemberRepository;
import jpabook.jpashop.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
class MemberServiceTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void signUp() {
        // given
        final String name = "kim";
        final String city = "city";
        final String street = "street";
        final String zipcode = "zipcode";

        Address address = Address.builder()
                .city(city)
                .street(street)
                .zipcode(zipcode)
                .build();

        Member member = Member.builder()
                .name(name)
                .address(address)
                .build();

        // when
        Long saveMemberId = memberService.signUp(member);
        // close(); // 해당 메서드 사용시 컨텍스트를 초기화하기 때문에 아래 동일성을 보장해주지 않는다. (따로 toString 메서드를 구현하면 가능)

        // then
        Member findMember = memberRepository.findById(saveMemberId)
                .orElse(null);
        assertThat(member).isEqualTo(findMember); // JPA 는 같은 영속성 컨텍스트에 관리되면서 pk 값이 같으면 동일성을 보장해준다.
    }

    @Test
    void fail_findMembers() {
        // given
        Member member1 = Member.builder()
                .name("name")
                .build();
        Member member2 = Member.builder()
                .name("name")
                .build();

        // when
        memberService.signUp(member1);

        // then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.signUp(member2);
        });

        assertThat(exception.getMessage()).isEqualTo("중복된 이름이 존재합니다.");
    }

    @Test
    void success_findMembers() {
        // given
        for (int i = 1; i <= 10; i++) {
            Member member = Member.builder()
                    .name("name" + i)
                    .build();
            memberService.signUp(member);
        }

        // when
        List<Member> findMembers = memberService.findMembers();

        // then
        assertThat(findMembers.size()).isEqualTo(10);
    }

    private void close() {
        entityManager.flush();
        entityManager.clear();
    }
}