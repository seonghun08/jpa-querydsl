package jpabook.jpashop.domain.order;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.order.OrderRepository;
import jpabook.jpashop.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
class OrderServiceTest {

    @Autowired EntityManager entityManager;
    @Autowired MemberRepository memberRepository;
    @Autowired ItemRepository itemRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    OrderService orderService;

    @Test
    void success_order() {
        // given
        final int price = 10000;
        final int stockQuantity = 10;
        final int orderCount = 2;

        Member member = createMember();
        Book book = createBook(price, stockQuantity);

        // when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // then
        Order order = orderRepository.findById(orderId)
                .orElse(null);

        assertThat(order).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ORDER);
        assertThat(order.getOrderItems().size()).isEqualTo(1);
        assertThat(order.getTotalPrice()).isEqualTo(price * orderCount);
        assertThat(book.getStockQuantity()).isEqualTo(stockQuantity - orderCount);
    }

    @DisplayName("상품주문 재고 수량 초과")
    @Test
    void fail_order() {
        // given
        final int price = 10000;
        final int stockQuantity = 10;
        final int orderCount = 11; // 재고 수량보다 많은 주문 수량

        Member member = createMember();
        Book book = createBook(price, stockQuantity);

        // when & then
        assertThrows(NotEnoughStockException.class, () ->
            orderService.order(member.getId(), book.getId(), orderCount));
    }

    @Test
    void cancelOrder() {
        // given
        final int price = 10000;
        final int stockQuantity = 10;
        final int orderCount = 2; // 재고 수량보다 많은 주문 수량

        Member member = createMember();
        Book book = createBook(price, stockQuantity);
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // when
        orderService.cancelOrder(orderId);
        close();

        // then
        Order order = orderRepository.findById(orderId)
                .orElse(null);
        Item item = itemRepository.findById(book.getId())
                .orElse(null);

        assertThat(order).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCEL);
        assertThat(item).isNotNull();
        assertThat(item.getStockQuantity()).isEqualTo(stockQuantity);
    }

    private Book createBook(int price, int stockQuantity) {
        Book book = new Book();
        book.setName("시골 JPA");
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        itemRepository.save(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member("회원", new Address("서울", "강가", "123-123"));
        memberRepository.save(member);
        return member;
    }

    private void close() {
        entityManager.flush();
        entityManager.clear();
    }
}