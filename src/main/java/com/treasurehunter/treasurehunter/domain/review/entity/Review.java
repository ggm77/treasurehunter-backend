package com.treasurehunter.treasurehunter.domain.review.entity;

import com.treasurehunter.treasurehunter.domain.post.entity.Post;
import com.treasurehunter.treasurehunter.domain.review.entity.image.ReviewImage;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class) // updatedAt을 위해서
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "review",
        indexes = {
                @Index(name = "idx_author_id", columnList = "author_id")
        }
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String title;

    @Column(length = 1000, nullable = false)
    private String content;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private int score;

    //리뷰 삭제되면 사진도 삭제
    @OneToMany(mappedBy = "review", orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();

    //후기가 적힐 게시글
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    //후기 작성한 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Builder
    public Review(
            final String title,
            final String content,
            final int score,
            final User author,
            final Post post
    ){
        this.title = title;
        this.content = content;
        this.score = score;
        this.author = author;
        this.post = post;
    }

    //연관 관계가 설정된 ReviewImage에서 URL만 가져오는 메서드
    public List<String> getImageUrls(){
        if(this.images == null || this.images.isEmpty()){
            return Collections.emptyList();
        }

        return this.images.stream()
                .sorted(Comparator.comparing(ReviewImage::getImageIndex))
                .map(ReviewImage::getUrl)
                .toList();
    }

    public void updateTitle(final String title) {
        this.title = title;
    }

    public void updateContent(final String content) {
        this.content = content;
    }

    public void updateScore(final int score) {
        //음수 예외처리
        if(score < 0){
            return;
        }

        this.score = score;
    }

    // 게시글과 연관 관계를 끊는 메서드
    public void detachPost(){
        this.post = null;
    }

    // 작성자와 연관 관게 끊는 메서드
    public void detachAuthor(){
        this.author = null;
    }
}
