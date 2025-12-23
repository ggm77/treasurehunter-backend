package com.treasurehunter.treasurehunter.domain.auth.oauth2.dto;

import com.treasurehunter.treasurehunter.domain.user.entity.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Oauth2ResponseDto {
    private final Role role;
    private final String accessToken;
    private final String tokenType;
    private final Long exprTime;
    private final String refreshToken;

    @Builder
    public Oauth2ResponseDto(
            final Role role,
            final String accessToken,
            final String tokenType,
            final Long exprTime,
            final String refreshToken
    ){
        this.role = role;
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.exprTime = exprTime;
        this.refreshToken = refreshToken;
    }
}
