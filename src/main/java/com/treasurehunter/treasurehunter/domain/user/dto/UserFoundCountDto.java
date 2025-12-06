package com.treasurehunter.treasurehunter.domain.user.dto;

import lombok.Getter;

@Getter
public class UserFoundCountDto {
    private final Long userId;
    private final Long foundCount;

    public UserFoundCountDto(
            final Long userId,
            final Long foundCount
    ){
        this.userId = userId;
        this.foundCount = foundCount;
    }
}
