package com.treasurehunter.treasurehunter.domain.user.domain;

import com.treasurehunter.treasurehunter.domain.post.domain.Post;
import com.treasurehunter.treasurehunter.domain.post.domain.like.PostLike;
import com.treasurehunter.treasurehunter.domain.review.domain.Review;
import com.treasurehunter.treasurehunter.domain.user.domain.oauth.UserOauth2Accounts;
import com.treasurehunter.treasurehunter.domain.userBadge.domain.UserBadge;
import com.treasurehunter.treasurehunter.global.auth.oauth.dto.UserOauth2AccountsRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @Column(length = 255, nullable = true)
    private String name;

    @Column(length = 15, nullable = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role;   // enum Role { USER, ADMIN, NOT_REGISTERED }

    //회원 등록일시 (자동 생성)
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserOauth2Accounts> userOauth2Accounts  = new ArrayList<>();

    //유저가 탈퇴해도 게시글 남기기 위해서 cascade와 orphanRemoval 둘다 끔
    @OneToMany(mappedBy = "author", orphanRemoval = false)
    private List<Post> posts = new ArrayList<>();

    //게시물 좋아요 구현을 위한 조인 테이블
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postLikes = new ArrayList<>();

    //탈퇴시에도 후기 남기기 위해 cascade, orphanRemoval 둘다 끔
    @OneToMany(mappedBy = "author", orphanRemoval = false)
    private List<Review> reviews = new ArrayList<>();

    //유저가 얻은 뱃지에 대한 기록 저장하는 엔티티와 연관관계 설정
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<UserBadge> userBadges = new ArrayList<>();

    //oauth 회원가입용 생성자
    public User(
            final UserOauth2AccountsRequestDto userOauth2AccountsRequestDto
    ){
        this.nickname = "temp";
        this.profileImage = userOauth2AccountsRequestDto.getProfileImage();
        this.name = "temp";
        this.role = Role.NOT_REGISTERED;
        this.point = 0;
        this.returnedItemsCount = 0;
        this.badgeCount = 0;
        this.totalScore = 0;
        this.totalReviews = 0;
    }

    //닉네임 변경
    public void updateNickname(final String newNickname){
        this.nickname = newNickname;
    }

    //프로필 사진 변경
    public void updateProfileImage(final String newProfileImage){
        this.profileImage = newProfileImage;
    }

    //이름 변경
    public void updateName(final String newName){
        this.name = newName;
    }

    //전화번호 변경
    public void updatePhoneNumber(final String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    //role을 본인 인증 되지 않은 유저로 변경
    public void updateRoleToNotVerified(){
        this.role = Role.NOT_VERIFIED;
    }

    //role을 일반 유저로 변경
    public void updateRoleToUser(){
        this.role = Role.USER;
    }

    public void addPoint(final int point){
        //불필요한 갱신 방지
        if(point <= 0){
            return;
        }
        this.point += point;
    }

    //게시글 수정시 포인트를 변경 할 때 쓰는 메서드
    public void adjustPointForPostUpdate(final int oldPoint, final int newPoint){
        final int updatePoint = this.point + oldPoint - newPoint;
        if(updatePoint < 0){
            throw new IllegalArgumentException("보유 포인트 부족");
        }
        this.point = updatePoint;
    }

    //파라미터 값 만큼 포인트 소비
    public void consumePoint(final int point){
        //포인트 부족하면 예외 처리
        if(this.point < point){
            throw new IllegalArgumentException("보유 포인트 부족");
        }

        this.point -= point;
    }

    //totalScore에 일정 수치만큼 증가
    public void increaseTotalScore(final int score){
        //불필요한  갱신 방지
        if(score <= 0){
            return;
        }

        this.totalScore += score;
    }

    //totalReviews를 1증가
    public void incrementTotalReviews(){
        this.totalReviews += 1;
    }
}
