package jpabook.jpashop.domain.item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Book extends Item {

    @Column(name = "author")
    private String author;

    @Column(name = "isbn")
    private String isbn;
}
