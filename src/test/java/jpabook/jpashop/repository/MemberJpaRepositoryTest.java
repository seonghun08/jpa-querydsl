package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired EntityManager em;
    @Autowired MemberRepository memberRepository;
    @Autowired MemberJpaRepository memberJpaRepository;

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

    private void close() {
        em.flush();
        em.clear();
    }
}