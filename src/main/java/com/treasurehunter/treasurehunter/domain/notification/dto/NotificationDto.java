package com.treasurehunter.treasurehunter.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class NotificationDto {
    private final Long targetUserId;
    private final String token;
    private final String title;
    private final String body;

    @Builder
    public NotificationDto(
            final Long targetUserId,
            final String token,
            final String title,
            final String body
    ){
        this.targetUserId = targetUserId;
        this.token = token;
        this.title = title;
        this.body = body;
    }
}
