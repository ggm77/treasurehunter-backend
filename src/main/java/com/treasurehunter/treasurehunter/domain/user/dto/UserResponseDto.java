package com.treasurehunter.treasurehunter.domain.user.dto;

import com.treasurehunter.treasurehunter.domain.post.entity.like.PostLike;
import com.treasurehunter.treasurehunter.domain.post.dto.PostSimpleResponseDto;
import com.treasurehunter.treasurehunter.domain.review.dto.ReviewResponseDto;
import com.treasurehunter.treasurehunter.domain.user.entity.Role;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.global.auth.oauth.dto.UserOauth2AccountsResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
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
    private final String lat;
    private final String lon;
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
    private UserResponseDto(
            final Long id,
            final String nickname,
            final String profileImage,
            final String name,
            final String phoneNumber,
            final Role role,
            final BigDecimal lat,
            final BigDecimal lon,
            final LocalDateTime createdAt,
            final Integer point,
            final Integer returnedItemsCount,
            final Integer badgeCount,
            final Integer totalScore,
            final Integer totalReviews,
            final List<UserOauth2AccountsResponseDto> userOauth2Accounts,
            final List<ReviewResponseDto> receivedReviews,
            final List<ReviewResponseDto> reviews,
            final List<PostSimpleResponseDto> posts,
            final List<PostSimpleResponseDto> likedPosts
    ) {
        this.id = id;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.lat = lat.toPlainString();
        this.lon = lon.toPlainString();
        this.createdAt = createdAt;
        this.point = point;
        this.returnedItemsCount = returnedItemsCount;
        this.badgeCount = badgeCount;
        this.totalScore = totalScore;
        this.totalReviews = totalReviews;
        this.userOauth2Accounts = userOauth2Accounts;
        this.receivedReviews = receivedReviews;
        this.reviews = reviews;
        this.posts = posts;
        this.likedPosts = likedPosts;
    }


    public UserResponseDto(final User user){
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
        this.name = user.getName();
        this.phoneNumber = user.getPhoneNumber();
        this.role = user.getRole();
        this.lat = user.getLat().toPlainString();
        this.lon = user.getLon().toPlainString();
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

    //개인정보 삭제하기 위한 메서드
    public UserResponseDto removeSensitiveData(){
        return UserResponseDto.builder()
                .id(this.id)
                .nickname(this.nickname)
                .profileImage(this.profileImage)
                .name(null)
                .phoneNumber(null)
                .role(this.role)
                .lat(null)
                .lon(null)
                .createdAt(this.createdAt)
                .point(this.point)
                .returnedItemsCount(this.returnedItemsCount)
                .badgeCount(this.badgeCount)
                .totalScore(this.totalScore)
                .totalReviews(this.totalReviews)
                .userOauth2Accounts(null)
                .receivedReviews(this.receivedReviews)
                .reviews(this.reviews)
                .posts(this.posts)
                .likedPosts(this.likedPosts)
                .build();
    }
}
