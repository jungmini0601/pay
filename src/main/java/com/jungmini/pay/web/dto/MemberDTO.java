package com.jungmini.pay.web.dto;

import com.jungmini.pay.domain.Member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class MemberDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateMemberRequest {

        @NotBlank(message = "이름은 필수 입력 값입니다.")
        @Size(min = 1, max = 255, message = "이름은 1~255자리  문자열 입니다.")
        private String name;

        @Email(message = "이메일 형식으로 입력 해주세요")
        @NotBlank(message = "이름은 필수 입력 값입니다.")
        @Size(min = 3, max = 255, message = "이메일은 3~255자리 문자열 입니다.")
        private String email;

        @NotBlank(message = "이름은 필수 입력 값입니다.")
        @Size(min = 3, max = 255, message = "비밀번호는 3~255자리 문자열 입니다.")
        private String password;

        public Member toEntity() {
            return Member.builder()
                    .name(this.name)
                    .email(this.email)
                    .password(this.password)
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateMemberResponse {

        private String email;

        private String name;

        private LocalDateTime createdAt;

        public static CreateMemberResponse from(Member member) {
            return CreateMemberResponse.builder()
                    .name(member.getName())
                    .email(member.getEmail())
                    .createdAt(member.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SigninMemberRequest {
        @Email(message = "이메일 형식으로 입력 해주세요")
        @NotBlank(message = "이름은 필수 입력 값입니다.")
        @Size(min = 3, max = 255, message = "이메일은 3~255자리 문자열 입니다.")
        private String email;

        @NotBlank(message = "이름은 필수 입력 값입니다.")
        @Size(min = 3, max = 255, message = "비밀번호는 3~255자리 문자열 입니다.")
        private String password;

        public Member toEntity() {
            return Member.builder()
                    .email(this.email)
                    .password(this.password)
                    .build();
        }
    }
}
