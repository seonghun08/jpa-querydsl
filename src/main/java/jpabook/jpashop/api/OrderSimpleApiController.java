package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.order.OrderRepository;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import jpabook.jpashop.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 연관관계 방향 설정
 * Order
 * Order -> Member
 * Order -> Delivery
 * -
 * 쿼리 방식 선택 권장 순서
 * 1. 우선 엔티티를 DTO 로 변환하는 방법을 선택한다.
 * 2. 필요하면 fetch join 으로 성능을 최적화한다. -> 대부분의 성능 이슈는 여기서 해결 됨
 * 3. 그래도 안된다면 DTO 로 직접 조회하는 방법을 사용한다.
 * 4. 최후의 방법은 JPA 가 제공하는 네이티브 SQL 이나 스프링 JDBC Template 을 사용하여 SQL 을 직접 사용한다.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderSimpleApiController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * 엔티티를 직접 꺼내서 사용한다.
     * 해당 방법은 실무에서 사용해선 안되며 이유는 다음과 같다.
     * 1. 스펙이 변경될 경우, 엔티티를 수정해야하는 문제가 생긴다.
     * 2. 민감한 정보까지 노출될 수 있어 위험하다.
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        return orderService.searchOrders(new OrderSearch());
    }

    /**
     * 엔티티를 조회하여 필요한 정보만 DTO 에 담는다.
     * Order 객체에 Member 와 Delivery 를 꺼낸다.
     * 리스트를 순회하면서 꺼내는 방식이기 때문에 비효율적이다.
     * 또한 N + 1 문제로 인해 무분별한 쿼리가 발생되는 단점을 가진다.
     */
    @GetMapping("/api/v2/simple-orders")
    public ResponseEntity<Result<List<SimpleOrderDto>>> ordersV2() {
        List<Order> orders = orderService.searchOrders(new OrderSearch());

        // List<SimpleOrderDto> list = new ArrayList<>();
        // for (Order order : orders) {
        //     SimpleOrderDto simpleOrderDto = new SimpleOrderDto(
        //             order.getId(),
        //             order.getMember().getName(),
        //             order.getOrderDate(),
        //             order.getStatus(),
        //             order.getDelivery().getAddress());
        //     list.add(simpleOrderDto);
        // }

        /*
         * N + 1 문제 발생
         * 1 + 회원 N개 + 배송 N개
         */
        List<SimpleOrderDto> list = orders.stream()
                .map(SimpleOrderDto::new)
                .toList();

        return ResponseEntity.ok(new Result<>(list.size(), list));
    }

    /**
     * v2의 N + 1 문제를 해결하기 위해 fetch join 을 사용한다.
     * fetch join 을 통해 Member 와 Delivery 를 한번에 가져올 수 있어 성능 최적화에 좋다.
     * 쿼리가 단 1번만 발생
     */
    @GetMapping("/api/v3/simple-orders")
    public ResponseEntity<Result<List<SimpleOrderDto>>> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberAndDelivery();
        List<SimpleOrderDto> list = orders.stream()
                .map(SimpleOrderDto::new)
                .toList();
        return ResponseEntity.ok(new Result<>(list.size(), list));
    }

    /**
     * JPQL 쿼리의 결과를 즉시 DTO 로 변환한다.
     * SELECT 절에서 원하는 데이터를 직접 선택하므로, 애플리케이션 네트워크 용량 최적화 (그러나 생각보다 미비한 수준)
     * Repository 의 재사용성이 떨어진다는 장점이 있다.
     * -
     * 결론적으로 V3 버전을 사용하는 것을 추천한다.
     * V4와 V3의 성능 차이가 크게 없어 우선적으로 V3를 사용하고 별도로 필요하다면 V4를 사용
     */
    @GetMapping("/api/v4/simple-orders")
    public ResponseEntity<Result<List<OrderSimpleQueryDto>>> ordersV4() {
        List<OrderSimpleQueryDto> orderDtos = orderSimpleQueryRepository.findOrderDtos();
        return ResponseEntity.ok(new Result<>(orderDtos.size(), orderDtos));
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        long count;
        T data;
    }

    @Data
    @AllArgsConstructor
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName(); // LAZY 초기화
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }
}
