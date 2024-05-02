package jpabook.jpashop.domain.item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;

@Entity
@Getter
public class Album extends Item{

    @Column(name = "artist")
    private String artist;

    @Column(name = "etc")
    private String etc;
}
