package com.treasurehunter.treasurehunter.domain.user.dto;

import com.treasurehunter.treasurehunter.domain.post.entity.Post;
import com.treasurehunter.treasurehunter.domain.post.entity.like.PostLike;
import com.treasurehunter.treasurehunter.domain.post.dto.PostSimpleResponseDto;
import com.treasurehunter.treasurehunter.domain.review.dto.ReviewResponseDto;
import com.treasurehunter.treasurehunter.domain.user.entity.Role;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.global.auth.oauth.dto.UserOauth2AccountsResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final List<ReviewResponseDto> receivedReviews;
    private final List<ReviewResponseDto> reviews;
    private final List<PostSimpleResponseDto> posts;
    private final List<PostSimpleResponseDto> likedPosts;
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

        this.receivedReviews = user.getReceivedReviews().stream()
                .map(ReviewResponseDto::new)
                .toList();

        this.reviews = user.getReviews().stream()
                .map(ReviewResponseDto::new)
                .toList();

        this.posts = user.getPosts().stream()
                .map(PostSimpleResponseDto::new)
                .toList();

        //정상적인 방법 찾기
        this.likedPosts = user.getPostLikes().stream()
                .map(PostLike::getPost)
                .filter(Objects::nonNull)
                .map(PostSimpleResponseDto::new)
                .toList();
    }
}
