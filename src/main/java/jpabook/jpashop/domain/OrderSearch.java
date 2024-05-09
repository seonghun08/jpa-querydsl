package jpabook.jpashop.domain;

import lombok.Data;

@Data
public class OrderSearch {

    String memberName;
    OrderStatus orderStatus;
}