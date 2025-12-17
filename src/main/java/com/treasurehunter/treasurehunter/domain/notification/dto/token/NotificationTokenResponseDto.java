package com.treasurehunter.treasurehunter.domain.notification.dto.token;

import com.treasurehunter.treasurehunter.domain.notification.entity.PlatformType;
import com.treasurehunter.treasurehunter.domain.user.dto.UserSimpleResponseDto;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationTokenResponseDto {

    private final Long id;
    private final String token;
    private final UserSimpleResponseDto user;
    private final PlatformType platform;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder
    public NotificationTokenResponseDto(
            final Long id,
            final String token,
            final User user,
            final PlatformType platform,
            final LocalDateTime createdAt,
            final LocalDateTime updatedAt
    ){
        this.id = id;
        this.token = token;
        this.user = new UserSimpleResponseDto(user);
        this.platform = platform;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
