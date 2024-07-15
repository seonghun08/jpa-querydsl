package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.member.MemberQuerydslRepository;
import jpabook.jpashop.repository.member.MemberRepository;
import jpabook.jpashop.repository.member.dto.MemberSearchCondition;
import jpabook.jpashop.repository.member.dto.MemberTeamDto;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final MemberQuerydslRepository memberQuerydslRepository;

    @GetMapping("/api/v1/members")
    public List<Member> findMembersV1() {
        return memberRepository.findAll();
    }

    @GetMapping("/api/v2/members")
    public Result findMembersV2() {
        List<MemberDto> collect = memberRepository.findAll()
                .stream()
                .map(m -> new MemberDto(m.getName()))
                .toList();
        return new Result<>(collect.size(), collect);
    }

    @GetMapping("/api/v1/member/{id}")
    public FindMemberResponse findMemberV1(@PathVariable("id") Long id) {
        Member findMember = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));
        return new FindMemberResponse(findMember.getId(), findMember.getName(), findMember.getAddress());
    }

    @PostMapping("api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long memberId = memberService.signUp(member);
        return new CreateMemberResponse(memberId);
    }

    @PostMapping("api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Long memberId = memberService.signUp(request.toEntity());
        return new CreateMemberResponse(memberId);
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id, request.getName());
        Member findMember = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @GetMapping("/api/v3/members")
    public List<MemberTeamDto> searchMemberTeam(MemberSearchCondition cond) {
        return memberQuerydslRepository.search(cond);
    }

    @GetMapping("/api/v1/search")
    public List<MemberTeamDto> searchV1(MemberSearchCondition cond) {
        return memberRepository.search(cond);
    }

    @GetMapping("/api/v2/search")
    public Page<MemberTeamDto> searchV2(MemberSearchCondition cond, Pageable pageable) {
        return memberRepository.searchSimple(cond, pageable);
    }

    @GetMapping("/api/v3/search")
    public Page<MemberTeamDto> searchV3(MemberSearchCondition cond, Pageable pageable) {
        return memberRepository.searchComplex(cond, pageable);
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        long count;
        T data;
    }

    @Data
    @AllArgsConstructor
    static class FindMemberResponse {
        private Long id;
        private String name;
        private Address address;
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    static class CreateMemberRequest {

        @NotEmpty(message = "이름은 필수 입력입니다.")
        private String name;

        public Member toEntity() {
            return Member.builder()
                    .name(name)
                    .build();
        }
    }

    @Data
    @AllArgsConstructor
    static class CreateMemberResponse {
        private Long id;
    }
}
