package com.treasurehunter.treasurehunter.domain.user.dto;

import com.treasurehunter.treasurehunter.domain.user.domain.Role;
import com.treasurehunter.treasurehunter.domain.user.domain.User;
import com.treasurehunter.treasurehunter.global.auth.oauth.dto.UserOauth2AccountsResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class UserResponseDto {
    private final Long id;
    private final String nickname;
    private final String profileImage;
    private final String name;
    private final String phoneNumber;
    private final Role role;
    private final LocalDateTime createdAt;
    private final Integer point;
    private final Integer returnedItemsCount;
    private final Integer badgeCount;
    private final Integer totalScore;
    private final Integer totalReviews;
    private final List<UserOauth2AccountsResponseDto> userOauth2Accounts;
    //각각 구현후 아래에 receivedReviews, reviews, posts, blockedUser 추가하기

    @Builder
    public UserResponseDto(final User user){
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
        this.name = user.getName();
        this.phoneNumber = user.getPhoneNumber();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
        this.point = user.getPoint();
        this.returnedItemsCount = user.getReturnedItemsCount();
        this.badgeCount = user.getBadgeCount();
        this.totalScore = user.getTotalScore();
        this.totalReviews = user.getTotalReviews();

        if(user.getUserOauth2Accounts() != null && !user.getUserOauth2Accounts().isEmpty()){
            this.userOauth2Accounts = user.getUserOauth2Accounts().stream()
                    .map(UserOauth2AccountsResponseDto::new)
                    .collect(Collectors.toList());
        } else{
            this.userOauth2Accounts = new ArrayList<>();
        }
    }
}
