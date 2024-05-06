package jpabook.jpashop.domain.member;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jpabook.jpashop.domain.Address;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager entityManager;

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

    private void close() {
        entityManager.flush();
        entityManager.clear();
    }
}