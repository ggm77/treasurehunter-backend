package com.treasurehunter.treasurehunter.domain.post.domain;

import com.treasurehunter.treasurehunter.domain.post.domain.image.PostImage;
import com.treasurehunter.treasurehunter.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class) // updatedAt을 위해서
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "post",
        indexes = {
                @Index(name = "idx_post_lat_lon", columnList = "lat, lon"),
                @Index(name = "idx_post_lat", columnList = "lat"),
                @Index(name = "idx_post_lon", columnList = "lon"),
                @Index(name = "idx_post_title", columnList = "title"),
                @Index(name = "idx_post_content", columnList = "content")
        }
)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String title;

    @Column(length = 1000, nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    private PostType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    //게시글 삭제되면 사진도 삭제
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    private int setPoint;

    @Enumerated(EnumType.STRING)
    private ItemCategory itemCategory;

    @Column(precision = 9, scale = 6)
    private BigDecimal lat;

    @Column(precision = 9, scale = 6)
    private BigDecimal lon;

    //찾거나 잃어버린 시점
    @Column(nullable = false)
    private LocalDateTime lostAt;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean isAnonymous;

    @Column(nullable = false)
    private boolean isCompleted;

    @Builder
    public Post(
            final String title,
            final String content,
            final PostType type,
            final User author,
            final int setPoint,
            final ItemCategory itemCategory,
            final BigDecimal lat,
            final BigDecimal lon,
            final LocalDateTime lostAt,
            final boolean isAnonymous,
            final boolean isCompleted
    ){
        this.title = title;
        this.content = content;
        this.type = type;
        this.author = author;
        this.setPoint = setPoint;
        this.itemCategory = itemCategory;
        this.lat = lat;
        this.lon = lon;
        this.lostAt = lostAt;
        this.isAnonymous = isAnonymous;
        this.isCompleted = isCompleted;
    }

    //후기 연관관계 추가

    public void updateTitle(final String title){
        this.title = title;
    }

    public void updateContent(final String content){
        this.content = content;
    }

    public void updateType(final PostType type){
        this.type = type;
    }

    public void updateImage(final List<PostImage> images){
        this.images = images;
    }

    public void updateSetPoint(final int setPoint){
        this.setPoint = setPoint;
    }

    public void updateItemCategory(final ItemCategory itemCategory){
        this.itemCategory = itemCategory;
    }

    public void updateLat(final BigDecimal lat){
        this.lat = lat;
    }

    public void updateLon(final BigDecimal lon){
        this.lon = lon;
    }

    public void updateLostAt(final LocalDateTime lostAt){
        this.lostAt = lostAt;
    }

    //익명인지 여부 true false 전환
    public void toggleIsAnonymous(){
        this.isAnonymous = !this.isAnonymous;
    }

    //완료 되었는지 여부 true false 전환
    public void toggleIsCompleted(){
        this.isCompleted = !this.isCompleted;
    }
}
