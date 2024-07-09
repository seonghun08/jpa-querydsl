package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.member.MemberJpaRepository;
import jpabook.jpashop.repository.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired EntityManager em;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MemberJpaRepository memberJpaRepository;

    @BeforeEach
    void beforeEach() {
        memberRepository.deleteAll();
    }

    @Test
    void testMember() {
        Member member = new Member("memberA");
        Member saveMember = memberJpaRepository.save(member);
        Member findMember = memberJpaRepository.find(saveMember.getId());

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getName()).isEqualTo(member.getName());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberJpaRepository.findById(member1.getId())
                .orElseThrow(RuntimeException::new);
        Member findMember2 = memberJpaRepository.findById(member2.getId())
                .orElseThrow(RuntimeException::new);
        assertThat(findMember1.getName()).isEqualTo(member1.getName());
        assertThat(findMember2.getName()).isEqualTo(member2.getName());

        // 리스트 조회 검증
        List<Member> members = memberJpaRepository.findAll();
        assertThat(members.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberJpaRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberJpaRepository.delete(member1);
        memberJpaRepository.delete(member2);

        long deletedCount = memberJpaRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    void paging() {
        saveMembers();

        final int age = 10;
        final int offset = 0;
        final int limit = 3;

        // memberRepository.findAll().forEach(m -> System.out.println("m = " + m));
        List<Member> members = memberJpaRepository.findByPage(age, offset, limit);
        long totalCount = memberJpaRepository.totalCount(age);

        members.forEach(m -> System.out.println("m = " + m));

        assertThat(members.size()).isEqualTo(3);
        assertThat(totalCount).isEqualTo(9);
    }

    @Test
    void bulkUpdate() {
        // given
        memberJpaRepository.save(new Member("member1", 10));
        memberJpaRepository.save(new Member("member2", 25));
        memberJpaRepository.save(new Member("member3", 30));
        memberJpaRepository.save(new Member("member4", 20));
        memberJpaRepository.save(new Member("member5", 11));

        // when
        int resultCount = memberJpaRepository.bulkAgePlus(20);

        // then
        assertThat(resultCount).isEqualTo(3);
    }

    private void saveMembers() {
        for (int i = 1; i < 10; i++) {
            memberJpaRepository.save(new Member("member" + i, 10));
        }
    }

    private void close() {
        em.flush();
        em.clear();
    }
}