package jpabook.jpashop.repository.order;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryExtensionImpl implements OrderRepositoryExtension{

    private final EntityManager em;

    @Override
    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.simplequery.OrderSimpleQueryDto(" + "o.id, m.name, o.orderDate, o.status, d.address) " +
                                " from Order o" +
                                " join o.member m" +
                                " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
    }
}
