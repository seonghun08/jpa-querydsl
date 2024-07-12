package jpabook.jpashop;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.QMember;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ApplicationTests {

	@Autowired EntityManager em;

	@Test
	void contextLoads() {
		Member member = new Member("user");
		em.persist(member);

		JPAQueryFactory query = new JPAQueryFactory(em);

		Member findMember = query
				.selectFrom(QMember.member)
				.fetchOne();

		assert findMember != null;

		assertThat(member).isEqualTo(findMember);
		assertThat(member.getId()).isEqualTo(findMember.getId());
	}

}
