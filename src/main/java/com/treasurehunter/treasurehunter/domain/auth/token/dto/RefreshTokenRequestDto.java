package com.treasurehunter.treasurehunter.domain.auth.token.dto;

import lombok.Getter;

@Getter
public class RefreshTokenRequestDto {
    private String refreshToken;
}
