package com.jungmini.pay.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member extends BaseTimeEntity {

    @Id
    private String email;

    @Column
    private String password;

    @Column
    private String name;

    public Member encodePassword(String encryptedPassword) {
        return Member.builder()
                .email(this.email)
                .password(encryptedPassword)
                .name(this.name)
                .build();
    }
}
