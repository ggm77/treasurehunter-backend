package com.treasurehunter.treasurehunter.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class NotificationDto {
    private final Long targetUserId;
    private final String token;
    private final String title;
    private final String body;
    private final String url;
    private final String profileImage;

    @Builder
    public NotificationDto(
            final Long targetUserId,
            final String token,
            final String title,
            final String body,
            final String url,
            final String profileImage
    ){
        this.targetUserId = targetUserId;
        this.token = token;
        this.title = title;
        this.body = body;
        this.url = url;
        this.profileImage = profileImage;
    }
}
