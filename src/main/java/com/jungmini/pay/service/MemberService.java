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
    public Member signUp(final Member member) {
        checkDuplicatedEmail(member.getEmail());
        final String encodedPassword = passwordEncoder.encode(member.getPassword());
        final Member passwordEncodedMember = member.encodePassword(encodedPassword);
        return memberRepository.save(passwordEncodedMember);
    }

    private void checkDuplicatedEmail(final String email) {
        if(memberRepository.findById(email).isPresent()) {
            throw new PayException(ErrorCode.MEMBER_DUPLICATED);
        }
    }

    /**
     * 읽기 전용으로 트랜잭션을 걸면 스프링 프레임워크가 하이버네이트 세션 플러시 모드를 MANUAL로 설정한다.
     * 강제로 플러시를 호출하지 않는 한 플러시가 일어나지 않는다.
     * 엔티티의 등록 수정 삭제가 동작하지 않고 변경 감지를 위한 스냅샷을 사용하지 않아서 성능이 향상된다.
     */
    public String signin(final Member signinRequestMember) {
        Member findedMember = memberRepository.findById(signinRequestMember.getEmail())
                .orElseThrow(() -> new PayException(ErrorCode.MEMBER_NOT_FOUND));

        checkPassword(signinRequestMember.getPassword(), findedMember.getPassword());
        return tokenService.generateToken(findedMember.getEmail());
    }

    private void checkPassword(String plainPassword, String encodedPassword) {
        if (!passwordEncoder.verify(plainPassword, encodedPassword)) {
            throw new PayException(ErrorCode.PASSWORD_MISMATCH);
        }
    }
}
