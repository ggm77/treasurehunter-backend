package com.treasurehunter.treasurehunter.domain.user.entity.oauth;

import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.global.auth.oauth.dto.UserOauth2AccountsRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserOauth2Accounts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //google, apple...
    @Column(length = 20, nullable = false)
    private String provider;

    //google에서의 sub, naver의 id같이 oauth에서 쓰는 식별자
    @Column(length = 255, nullable = false)
    private String providerUserId;

    @Column(length = 320, nullable = true)
    private String email;

    @Column(length = 255, nullable = true)
    private String name;

    @Column(length = 2048, nullable = true)
    private String profileImage;

    //oauth에서 제공하는 엑세스 토큰 (unlink시 필요함)
    @Column(length = 4100, nullable = false)
    private String accessToken;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime linkedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public UserOauth2Accounts(
            final UserOauth2AccountsRequestDto userOauth2AccountsRequestDto,
            final User user
    ){
        this.provider = userOauth2AccountsRequestDto.getProvider();
        this.providerUserId = userOauth2AccountsRequestDto.getProviderUserId();
        this.email = userOauth2AccountsRequestDto.getEmail();
        this.name = userOauth2AccountsRequestDto.getName();
        this.profileImage = userOauth2AccountsRequestDto.getProfileImage();
        this.accessToken = userOauth2AccountsRequestDto.getAccessToken();
        this.user = user;
    }
}
