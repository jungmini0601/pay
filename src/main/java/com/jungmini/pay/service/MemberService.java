package com.jungmini.pay.service;

import com.jungmini.pay.domain.Member;
import com.jungmini.pay.exception.ErrorCode;
import com.jungmini.pay.exception.PayException;
import com.jungmini.pay.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member signUp(final Member member) {
        checkDuplicatedEmail(member.getEmail());
        final String encodedPassword = passwordEncoder.encode(member.getPassword());
        final Member passwordEncodedMember = member.encodePassword(encodedPassword);
        memberRepository.save(passwordEncodedMember);
        return passwordEncodedMember;
    }

    private void checkDuplicatedEmail(final String email) {
        if(memberRepository.findById(email).isPresent()) {
            throw new PayException(ErrorCode.MEMBER_DUPLICATED);
        }
    }
}
