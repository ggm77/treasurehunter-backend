package com.treasurehunter.treasurehunter.domain.user.dto;

import com.treasurehunter.treasurehunter.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserLeaderboardResponseDto {

    private final Long id;
    private final String nickname;
    private final String profileImage;
    private final Integer totalScore;
    private final Integer totalReviews;
    private final Integer returnedItemsCount;
    private final Integer point;
    private final Integer foundCount;
    private final Integer badgeCount;

    @Builder
    public UserLeaderboardResponseDto(
            final User user,
            final Integer foundCount
    ) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
        this.totalScore = user.getTotalScore();
        this.totalReviews = user.getTotalReviews();
        this.returnedItemsCount = user.getReturnedItemsCount();
        this.point = user.getPoint();
        this.foundCount = foundCount;
        this.badgeCount = user.getBadgeCount();
    }
}
