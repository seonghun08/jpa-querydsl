package jpabook.jpashop.repository.query;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos(int offset, int limit) {
        List<OrderQueryDto> result = findOrders(offset, limit);
        result.forEach(o -> o.setOrderItems(findOrderItems(o.getOrderId())));
        return result;
    }

    public List<OrderQueryDto> findAllByDtoOptimization(int offset, int limit) {
        
        // OrderQueryDto 에 직접 조회된 엔티티를 담는다. 
        List<OrderQueryDto> result = findOrders(offset, limit);
        
        // 조회된 DTO 에 포함된 ORDER_ID 를 List 형식으로 뽑는다. (DTO 에는 orderItems 에 값은 현재 담겨져 있지 않음)
        List<Long> orderIds = result.stream()
                .map(OrderQueryDto::getOrderId)
                .toList();

        // orderIds 를 IN 절을 활용하여 OrderItemQueryDto 에 담는다. 
        List<OrderItemQueryDto> orderItems = em.createQuery(
                        "select new jpabook.jpashop.repository.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        // OrderQueryDto 에 OrderItemQueryDto 를 매칭하기 위해 orderId 를 key 로 만들고, OrderItemQueryDto 를 value 에 담는다.
        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));

        // result 를 순회하며, orderId 를 매칭시켜 orderItem 를 담는다.
        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        // 결과 반환
        return result;
    }

    public List<OrderFlatDto> findAllByDtoFlat(int offset, int limit) {
        List<OrderFlatDto> resultList = em.createQuery(
                        "select new jpabook.jpashop.repository.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                                " from Order o" +
                                " join o.member m" +
                                " join o.delivery d" +
                                " join o.orderItems oi" +
                                " join oi.item i", OrderFlatDto.class)
                .getResultList();
        return resultList;
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    private List<OrderQueryDto> findOrders(int offset, int limit) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDto.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
