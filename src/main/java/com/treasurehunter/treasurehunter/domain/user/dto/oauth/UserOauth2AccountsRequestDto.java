package com.treasurehunter.treasurehunter.domain.user.dto.oauth;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserOauth2AccountsRequestDto {
    private final String provider;
    private final String providerUserId;
    private final String email;
    private final String name;
    private final String profileImage;
    private final String refreshToken;

    @Builder
    public UserOauth2AccountsRequestDto(
            final String provider,
            final String providerUserId,
            final String email,
            final String name,
            final String profileImage,
            final String refreshToken
    ){
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.email = email;
        this.name = name;
        this.profileImage = profileImage;
        this.refreshToken = refreshToken;
    }
}
