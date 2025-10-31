package com.treasurehunter.treasurehunter.domain.userBadge.domain;

import com.treasurehunter.treasurehunter.domain.admin.badge.domain.Badge;
import com.treasurehunter.treasurehunter.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 뱃지 엔티티와 유저 엔티티를 이어주는 엔티티 이면서,
 * 동시에 뱃지를 받은 이력을 저장하는 엔티티이다.
 */
@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBadge {

    //유저가 뱃지 얻은 기록의 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime earnedDate;

    //생성일 (자동으로 채워짐)
    @CreationTimestamp
    private LocalDateTime createdAt;

    //마지막 수정일 (자동으로 업데이트)
    @LastModifiedDate
    private LocalDateTime updatedAt;

    //뱃지를 얻은 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    //유저가 얻은 뱃지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id")
    private Badge badge;

    @Builder
    public UserBadge(
            final LocalDateTime earnedDate,
            final User user,
            final Badge badge
    ){
        this.earnedDate = earnedDate;
        this.user = user;
        this.badge = badge;
    }
}
