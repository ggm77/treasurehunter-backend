package com.treasurehunter.treasurehunter.domain.admin.badge.entity;

import com.treasurehunter.treasurehunter.domain.userBadge.entity.UserBadge;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Badge {

    //뱃지 고유 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //뱃지 이름
    @Column(length = 255, nullable = false)
    private String name;

    //뱃지 설명
    @Column(length = 255, nullable = false)
    private String description;

    //생성일 (자동으로 채워짐)
    @CreationTimestamp
    private LocalDateTime createdAt;

    //마지막 수정일 (자동으로 업데이트 됨)
    @LastModifiedDate
    private LocalDateTime updatedAt;

    //유저가 뱃지를 얻은 기록 저장하는 엔티티와 연관관계 설정
    @OneToMany(mappedBy = "badge")
    private List<UserBadge> userBadges = new ArrayList<>();

    @Builder
    public Badge(
            final String name,
            final String description
    ){
        this.name = name;
        this.description = description;
    }

    //뱃지 이름 수정 메서드
    public void updateName(final String name){
        this.name = name;
    }

    //뱃지 정보 수정 메서드
    public void updateDescription(final String description){
        this.description = description;
    }
}
