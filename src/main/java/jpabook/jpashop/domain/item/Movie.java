package jpabook.jpashop.domain.item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;

@Entity
@Getter
public class Movie extends Item {

    @Column(name = "director")
    private String director;

    @Column(name = "actor")
    private String actor;
}
