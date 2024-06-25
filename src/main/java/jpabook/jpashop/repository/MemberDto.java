package jpabook.jpashop.repository;

import lombok.Data;

@Data
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
