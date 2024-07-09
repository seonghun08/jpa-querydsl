package jpabook.jpashop.repository.member;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MemberDto {

    private Long id;
    private String name;
    private String teamName;

    public MemberDto(Long id, String name, String teamName) {
        this.id = id;
        this.name = name;
        this.teamName = teamName;
    }
}
