package jpabook.jpashop.repository;

import jpabook.jpashop.repository.simplequery.OrderSimpleQueryDto;

import java.util.List;

public interface OrderRepositoryExtension {

    List<OrderSimpleQueryDto> findOrderDtos();
}
