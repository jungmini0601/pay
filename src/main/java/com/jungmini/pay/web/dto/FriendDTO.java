package com.jungmini.pay.web.dto;

import com.jungmini.pay.domain.FriendRequest;
import com.jungmini.pay.domain.Member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class FriendDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateFriendRequest {
        @Email(message = "이메일 형식으로 입력 해주세요")
        @NotBlank(message = "이름은 필수 입력 값입니다.")
        @Size(min = 3, max = 255, message = "이메일은 3~255자리 문자열 입니다.")
        private String email;

        public FriendRequest toEntity() {
            return FriendRequest.builder()
                    .recipient(Member.builder().email(email).build())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateFriendResponse {

        private String message;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FindFriendRequestResponse {
        private long id;
        private String email;
        private String name;
        private LocalDateTime createdAt;

        public static FindFriendRequestResponse from(FriendRequest friendRequest) {
            return FindFriendRequestResponse.builder()
                    .id(friendRequest.getId())
                    .email(friendRequest.getRequester().getEmail())
                    .name(friendRequest.getRequester().getName())
                    .createdAt(friendRequest.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AcceptFriendRequestResponse {
        private String message;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DenyFriendRequestResponse {
        private String message;
    }
}
