package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Member;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberResponse {

    private String name;

    public MemberResponse(Member member) {
        this.name = member.getName();
    }
}
