package com.jungmini.pay.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false) // 부모 필드 값은 확인 안하도록 설정
@Entity
public class Member extends BaseTimeEntity {

    @Id
    private String email;

    @Column
    @EqualsAndHashCode.Exclude private String password;

    @Column
    @EqualsAndHashCode.Exclude private String name;

    public Member encodePassword(String encryptedPassword) {
        return Member.builder()
                .email(this.email)
                .password(encryptedPassword)
                .name(this.name)
                .build();
    }
}
