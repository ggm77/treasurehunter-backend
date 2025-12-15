package com.treasurehunter.treasurehunter.domain.user.dto;

import com.treasurehunter.treasurehunter.domain.user.entity.User;
import lombok.Getter;

@Getter
public class UserChatRoomResponseDto {

    private final Long id;
    private final String nickname;
    private final String profileImage;
    private final Integer totalScore;
    private final Integer totalReviews;
    private final Boolean isCaller;

    public UserChatRoomResponseDto(
            final User user,
            final Boolean isCaller
    ) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
        this.totalScore = user.getTotalScore();
        this.totalReviews = user.getTotalReviews();
        this.isCaller = isCaller;
    }

    //익명 사용자
    public UserChatRoomResponseDto(final boolean isCaller) {
        this.id = 0L;
        this.nickname = null;
        this.profileImage = null;
        this.totalScore = 0;
        this.totalReviews = 0;
        this.isCaller = isCaller;
    }
}
