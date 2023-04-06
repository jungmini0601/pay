package com.jungmini.pay.web;

import com.jungmini.pay.domain.Member;

import com.jungmini.pay.web.dto.MemberDTO;
import com.jungmini.pay.service.MemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/members")
    public ResponseEntity<MemberDTO.CreateMemberResponse> signup(
            @RequestBody @Valid MemberDTO.CreateMemberRequest request) {

        Member member = memberService.signUp(request.toEntity());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MemberDTO.CreateMemberResponse.from(member));
    }

    @PostMapping("/members/signin")
    public ResponseEntity<Void> signin(
            @RequestBody @Valid MemberDTO.SigninMemberRequest request) {

        String token = memberService.signin(request.toEntity());

        return ResponseEntity.status(HttpStatus.OK)
                .header("Auth", token).build();
    }
}
