package jpabook.jpashop.api;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.order.OrderRepository;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import jpabook.jpashop.service.OrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> orders = orderService.searchOrders(new OrderSearch());
        orders.forEach(o -> {
            o.getMember().getName();
            o.getDelivery().getAddress();
            List<OrderItem> orderItems = o.getOrderItems();
            orderItems.forEach(oi -> oi.getItem().getName());
        });
        return orders;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderService.searchOrders(new OrderSearch());
        return orders.stream()
                .map(OrderDto::new)
                .toList();
    }

    /**
     * fetch join 을 사용할 경우 페이징이 불가능하다는 치명적인 단점을 가진다.
     * 컬렉션 fetch join 은 1개만 사용할 수 있으면 컬렉션 2개 이상을 fetch join 을 하면 안된다.
     * - 데이터가 부정합하게 조회될 수 있다.
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem(); // join fetch
        return orders.stream()
                .map(OrderDto::new)
                .toList();
    }

    /**
     * V3에서 페이징 쿼리 해결방법
     * ToOne 관계는 V3 처럼 fetch join 을 통해 최적화
     * 컬렉션은 지연 로딩으로 유지하고 'hibernate.default_batch_fetch_size', '@BatchSize' 를 통해 최적화
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_paging(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderSimpleQueryRepository.findAllWithMemberDelivery(offset, limit); // join fetch -> XxxToOne 관계만 사용
        return orders.stream()
                .map(OrderDto::new)
                .toList();
    }

    /**
     * JPA 에서 DTO 를 직접 조회한다.
     * ToOne 관계는 join 쿼리로 바로 조회 후, 컬렉션 n 개수 만큼 쿼리를 조회하여 채운다. (N + 1 문제 유사?)
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        return orderQueryRepository.findOrderQueryDtos(offset, limit);
    }

    /**
     * V4 의 문제점을 개선
     * 일대다 관계인 컬렉션은 IN 절을 활용하여 메모리에 미리 조회하여 최적화한다.
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        return orderQueryRepository.findAllByDtoOptimization(offset, limit);
    }

    /**
     * flat 데이터로 모두 가져온다. (쿼리 1번 발생)
     * join 으로 인한 중복 데이터를 애플리케이션 단계에서 걸러내는 방법이다.
     * 쿼리가 1번 발생되지만 중복 데이터가 추가되므로 상황에 따라 V5보다 더 느려질 수도 있다.
     * 또한 페이징이 불가능
     */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {

        List<OrderFlatDto> flats = orderQueryRepository.findAllByDtoFlat(offset, limit);

        Map<OrderQueryDto, List<OrderItemQueryDto>> collect = flats.stream()
                .collect(Collectors.groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        Collectors.mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()),
                                Collectors.toList())));

        Set<Map.Entry<OrderQueryDto, List<OrderItemQueryDto>>> entries = collect.entrySet();

        return entries.stream()
                .map(e -> new OrderQueryDto(
                        e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .toList();
    }

    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
            this.orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .toList();
        }
    }

    @Data
    static class OrderItemDto {
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            this.itemName = orderItem.getItem().getName();
            this.orderPrice = orderItem.getOrderPrice();
            this.count = orderItem.getCount();
        }
    }
}
