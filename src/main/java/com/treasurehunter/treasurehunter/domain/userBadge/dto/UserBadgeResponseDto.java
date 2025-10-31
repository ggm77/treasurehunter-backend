package com.treasurehunter.treasurehunter.domain.userBadge.dto;

import com.treasurehunter.treasurehunter.domain.user.dto.UserSimpleResponseDto;
import com.treasurehunter.treasurehunter.domain.userBadge.domain.UserBadge;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserBadgeResponseDto {
    private final Long id;
    private final String name;
    private final Long badgeId;
    private final LocalDateTime earnedDate;
    private final UserSimpleResponseDto owner;

    public UserBadgeResponseDto(final UserBadge userBadge) {
        this.id = userBadge.getId();
        //N+1 해결하기
        this.name = userBadge.getBadge().getName();
        //N+1 해결하기
        this.badgeId = userBadge.getBadge().getId();
        this.earnedDate = userBadge.getEarnedDate();
        //N+1 해결하기
        this.owner = new UserSimpleResponseDto(userBadge.getUser());
    }
}
