package com.treasurehunter.treasurehunter.domain.userBadge.dto.list;

import com.treasurehunter.treasurehunter.domain.userBadge.dto.UserBadgeResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class UserBadgeListResponseDto {
    private final List<UserBadgeResponseDto> badges;

    @Builder
    public UserBadgeListResponseDto(final List<UserBadgeResponseDto> badges) {
        this.badges = badges;
    }
}
