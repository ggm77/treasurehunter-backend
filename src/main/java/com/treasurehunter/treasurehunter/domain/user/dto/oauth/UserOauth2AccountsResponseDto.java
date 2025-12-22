package com.treasurehunter.treasurehunter.domain.user.dto.oauth;

import com.treasurehunter.treasurehunter.domain.user.entity.Role;
import com.treasurehunter.treasurehunter.domain.user.entity.oauth.UserOauth2Accounts;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserOauth2AccountsResponseDto {

    private final  Long id;
    private final Long userId;
    private final Role userRole;
    private final String provider;
    private final String providerUserId;
    private final String email;
    private final String name;
    private final String profileImage;
    private final LocalDateTime linkedAt;

    @Builder
    public UserOauth2AccountsResponseDto(
            final UserOauth2Accounts userOauth2Accounts
    ){
        this.id = userOauth2Accounts.getId();
        this.userId = userOauth2Accounts.getUser().getId();
        this.userRole = userOauth2Accounts.getUser().getRole();
        this.provider = userOauth2Accounts.getProvider();
        this.providerUserId = userOauth2Accounts.getProviderUserId();
        this.email = userOauth2Accounts.getEmail();
        this.name = userOauth2Accounts.getName();
        this.profileImage = userOauth2Accounts.getProfileImage();
        this.linkedAt = userOauth2Accounts.getLinkedAt();
    }
}
