package jpabook.jpashop.repository.order;

import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;

import java.util.List;

public interface OrderRepositoryExtension {

    List<OrderSimpleQueryDto> findOrderDtos();
}
