package jpabook.jpashop.domain.member;

import jakarta.persistence.*;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.BaseTimeEntity;
import jpabook.jpashop.domain.Order;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

    @Builder
    public Member(String name, Address address, List<Order> orders) {
        this.name = name;
        this.address = address;
        this.orders = orders;
    }
}
