package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryExtension {

    @Query("select o from Order o join fetch o.member m join fetch o.delivery d")
    List<Order> findAllWithMemberAndDelivery();

    @Query("select o from Order o join fetch o.member m join fetch o.delivery d join fetch o.orderItems oi join fetch oi.item i")
    List<Order> findAllWithItem();
}
