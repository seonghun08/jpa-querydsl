package jpabook.jpashop.repository.member;

import com.querydsl.core.annotations.QueryProjection;
import jpabook.jpashop.domain.Member;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDto {

    //private Long id;
    private Long memberId;
    private String name;
    private Integer age;
    private String teamName;

    public MemberDto(Long id, String name, String teamName) {
        this.memberId = id;
        this.name = name;
        this.teamName = teamName;
    }

    @QueryProjection
    public MemberDto(Long id, String name, Integer age, String teamName) {
        this.memberId = id;
        this.name = name;
        this.age = age;
        this.teamName = teamName;
    }

    public MemberDto(Member member) {
        this.memberId = member.getId();
        this.name = member.getName();
        this.teamName = null;
    }
}
