package com.treasurehunter.treasurehunter.domain.user.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {

    NOT_REGISTERED("ROLE_NOT_REGISTERED", "회원가입이 끝나지 않은 유저"),
    USER("ROLE_USER", "일반 사용자"),
    ADMIN("ROLE_ADMIN", "관리자");

    private String key;
    private String title;
}
