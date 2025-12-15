package com.treasurehunter.treasurehunter.domain.user.dto;

import com.treasurehunter.treasurehunter.domain.chat.entity.ChatUserType;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import lombok.Getter;

@Getter
public class UserChatRoomResponseDto {

    private final Long id;
    private final String nickname;
    private final String profileImage;
    private final Integer totalScore;
    private final Integer totalReviews;
    private final ChatUserType userType;

    public UserChatRoomResponseDto(
            final User user,
            final Boolean isCaller
    ) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
        this.totalScore = user.getTotalScore();
        this.totalReviews = user.getTotalReviews();
        if(isCaller) {
            this.userType = ChatUserType.CALLER;
        } else {
            this.userType = ChatUserType.AUTHOR;
        }
    }

    //익명 사용자
    public UserChatRoomResponseDto(final boolean isCaller) {
        this.id = 0L;
        this.nickname = null;
        this.profileImage = null;
        this.totalScore = 0;
        this.totalReviews = 0;
        if(isCaller) {
            this.userType = ChatUserType.CALLER;
        } else {
            this.userType = ChatUserType.AUTHOR;
        }
    }
}
