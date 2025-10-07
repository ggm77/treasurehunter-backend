package com.treasurehunter.treasurehunter.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DevJwtResponseDto {
    private final String accessToken;
    private final String tokenType;
    private final Long exprTime;
    private final String refreshToken;

    @Builder
    public DevJwtResponseDto(
            final String accessToken,
            final String tokenType,
            final Long exprTime,
            final String refreshToken
    ){
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.exprTime = exprTime;
        this.refreshToken = refreshToken;
    }
}
