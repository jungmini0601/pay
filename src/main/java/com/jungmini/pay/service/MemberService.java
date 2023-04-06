package com.jungmini.pay.service;

import com.jungmini.pay.domain.Member;

import com.jungmini.pay.common.exception.ErrorCode;
import com.jungmini.pay.common.exception.PayException;
import com.jungmini.pay.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional
    public Member signUp(Member member) {
        checkDuplicatedEmail(member.getEmail());
        String encodedPassword = passwordEncoder.encode(member.getPassword());
        Member passwordEncodedMember = member.encodePassword(encodedPassword);
        return memberRepository.save(passwordEncodedMember);
    }

    @Transactional(readOnly = true)
    public String signin(Member signinRequestMember) {
        Member findedMember = getFindedMember(signinRequestMember.getEmail());
        checkPassword(signinRequestMember.getPassword(), findedMember.getPassword());
        return tokenService.generateToken(findedMember.getEmail());
    }

    private Member getFindedMember(String email) {
        return memberRepository.findById(email)
                .orElseThrow(() -> new PayException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void checkPassword(String plainPassword, String encodedPassword) {
        if (!passwordEncoder.verify(plainPassword, encodedPassword)) {
            throw new PayException(ErrorCode.PASSWORD_MISMATCH);
        }
    }

    private void checkDuplicatedEmail(String email) {
        if(memberRepository.findById(email).isPresent()) {
            throw new PayException(ErrorCode.MEMBER_DUPLICATED);
        }
    }
}
