package com.example.account.domain;

import lombok.*;

import javax.persistence.Entity;

//회원
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class AccountUser extends BaseEntity {
    private String name; // 사용자명
}
