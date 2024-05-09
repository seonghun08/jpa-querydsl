package jpabook.jpashop.service;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;

import java.util.List;

public interface OrderService {

    Long order(Long memberId, Long itemId, int count);

    void cancelOrder(Long orderId);

    List<Order> searchOrders(OrderSearch orderSearch);
}
