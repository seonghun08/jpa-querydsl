package jpabook.jpashop.repository.member.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MemberTeamDto {

    private Long memberId;
    private String name;
    private int age;
    private Long teamId;
    private String teamName;

    @QueryProjection
    public MemberTeamDto(Long memberId, String name, int age, Long teamId, String teamName) {
        this.memberId = memberId;
        this.name = name;
        this.age = age;
        this.teamId = teamId;
        this.teamName = teamName;
    }
}
