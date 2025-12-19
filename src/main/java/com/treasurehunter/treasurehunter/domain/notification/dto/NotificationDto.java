package com.treasurehunter.treasurehunter.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationDto {
    private Long targetUserId;
    private String token;
    private String title;
    private String body;
    private String url;
    private String profileImage;

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
