package com.jungmini.pay.common.resolover;

import com.jungmini.pay.exception.ErrorCode;
import com.jungmini.pay.exception.PayException;
import com.jungmini.pay.repository.MemberRepository;
import com.jungmini.pay.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@RequiredArgsConstructor
@Component
public class SigninMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberRepository memberRepository;
    private final TokenService tokenService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(SigninMember.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String token = parseToken(request);
        checkAuthorization(token);
        String email = tokenService.verifyToken(token);
        return memberRepository.findById(email)
                .orElseThrow(() -> new PayException(ErrorCode.BAD_REQUEST));
    }

    private static String parseToken(HttpServletRequest request) {
        return request.getHeader("Auth");
    }

    private static void checkAuthorization(String token) {
        if (token == null || token.length() == 0) {
            throw new PayException(ErrorCode.UN_AUTHORIZED);
        }
    }
}
