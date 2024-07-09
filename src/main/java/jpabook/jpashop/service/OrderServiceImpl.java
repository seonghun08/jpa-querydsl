package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.DeliveryStatus;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.member.MemberRepository;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.order.OrderRepository;
import jpabook.jpashop.domain.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final EntityManager entityManager;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    @Override
    public Long order(Long memberId, Long itemId, int count) {

        // 엔티티 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("not found:" + memberId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("not found:" + itemId));

        // 배송정보 생성
        Delivery delivery = Delivery.builder()
                .address(member.getAddress())
                .status(DeliveryStatus.READY)
                .build();

        // 주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        // 주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        return orderRepository.save(order).getId();
    }

    @Override
    public void cancelOrder(Long orderId) {
        // 주문 엔티티 조회
        Order order = orderRepository.findById(orderId)
               .orElseThrow(() -> new IllegalArgumentException("not found: " + orderId));
        // 주문 취소
        order.cancel();

        // 엔티티 조회 + 주문 취소
        // orderRepository.findById(orderId)
        //         .ifPresent(Order::cancel);
    }

    @Override
    public List<Order> searchOrders(OrderSearch orderSearch) {
        //        return entityManager.createQuery("select o from Order o join Member m" +
        //                        " where o.status = :status " +
        //                        " and m.name like %:name%", Order.class)
        //                .setParameter("status", orderSearch.orderStatus())
        //                .setParameter("name", orderSearch.memberName())
        //                .setMaxResults(1000) // 최대 1000건
        //                .getResultList();
        return findAllByString(orderSearch);
    }

    public List<Order> findAllByString(OrderSearch orderSearch) {
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;

        // 주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            jpql += " where";
            isFirstCondition = false;
            jpql += " o.status = :status";
        }

        // 회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = entityManager.createQuery(jpql, Order.class)
                .setMaxResults(1000); // 최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }
}
