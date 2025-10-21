package com.treasurehunter.treasurehunter.domain.user.dto;

import com.treasurehunter.treasurehunter.domain.user.domain.User;
import lombok.Getter;

@Getter
public class UserSimpleResponseDto {

    private final Long id;
    private final String nickname;
    private final String profileImage;
    private final Integer totalScore;
    private final Integer totalReviews;

    public UserSimpleResponseDto(final User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
        this.totalScore = user.getTotalScore();
        this.totalReviews = user.getTotalReviews();
    }
}
