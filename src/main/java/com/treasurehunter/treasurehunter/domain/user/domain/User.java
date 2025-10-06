package com.treasurehunter.treasurehunter.domain.user.domain;

import com.treasurehunter.treasurehunter.domain.user.dto.UserRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 유저 정보를 저장하는 엔티티
 * Review, Post, PostLike, UserBlock, PurchaseHistory와 연관관계를 가짐
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    //PK 유저 고유 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //닉네임 (중복 허용 안됨)
    @Column(length = 255, nullable = false)
    private String nickname;

    //프로필 사진 url
    @Column(length = 255, nullable = true)
    private String profileImage;

    //실제 이름
    @Column(length = 255, nullable = false)
    private String name;

    //회원 등록일시 (자동 생성)
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    //등록한 oauth 종류
    @Column(length = 255, nullable = false)
    private String oauth;

    //oauth uid
    @Column(length = 255, nullable = false)
    private String uid;

    //사용자가 가진 포인트
    @Column(length = 255, nullable = false)
    private Integer point;

    //찾아준 물건 총 개수
    @Column(nullable = false)
    private Integer returnedItemsCount;

    //가진 뱃지 총 개수
    @Column(nullable = false)
    private Integer badgeCount;

    //받은 후기들의 총 점수 //totalReviews로 나눠서 후기 평균 점수 계산
    @Column(nullable = false)
    private Integer totalScore;

    //받은 후기의 총 개수 // totalScore/totalReviews 로 계산해서 후기 평균 점수 계산
    @Column(nullable = false)
    private Integer totalReviews;

    //회원가입용 생성자
    @Builder
    public User(final UserRequestDto userRequestDto){
        this.nickname = userRequestDto.getNickname();
        this.profileImage = userRequestDto.getProfileImage();
        this.name = userRequestDto.getName();
        this.oauth = userRequestDto.getOauth();
        this.uid = userRequestDto.getUid();
        this.point = 0;
        this.returnedItemsCount = 0;
        this.badgeCount = 0;
        this.totalScore = 0;
        this.totalReviews = 0;
    }

    //닉네임 변경
    public void changeNickname(final String newNickname){
        this.nickname = newNickname;
    }

    //프로필 사진 변경
    public void changeProfileImage(final String newProfileImage){
        this.profileImage = newProfileImage;
    }

    //이름 변경
    public void changeName(final String newName){
        this.name = newName;
    }
}
