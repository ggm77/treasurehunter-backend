package com.treasurehunter.treasurehunter.domain.user.dto;

import com.treasurehunter.treasurehunter.domain.user.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserResponseDto {
    private final Long id;
    private final String nickname;
    private final String profileImage;
    private final String name;
    private final Integer point;
    private final Integer returnedItemsCount;
    private final Integer badgeCount;
    private final Integer totalScore;
    private final Integer totalReviews;
    //각각 구현후 아래에 receivedReviews, reviews, posts, blockedUser 추가하기

    @Builder
    public UserResponseDto(final User user){
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
        this.name = user.getName();
        this.point = user.getPoint();
        this.returnedItemsCount = user.getReturnedItemsCount();
        this.badgeCount = user.getBadgeCount();
        this.totalScore = user.getTotalScore();
        this.totalReviews = user.getTotalReviews();
    }
}
