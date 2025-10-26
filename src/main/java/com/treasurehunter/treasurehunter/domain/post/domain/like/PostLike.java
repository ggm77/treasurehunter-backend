package com.treasurehunter.treasurehunter.domain.post.domain.like;

import com.treasurehunter.treasurehunter.domain.post.domain.Post;
import com.treasurehunter.treasurehunter.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        name = "post_like",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_post_like_user_post", columnNames = {"user_id", "post_id"})
        },
        indexes = {
                @Index(name = "idx_post_like_user_post", columnList = "user_id, post_id"),
                @Index(name = "idx_post_like_post_user", columnList = "post_id, user_id")
        }
)
public class PostLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Builder
    public PostLike(
            final User user,
            final Post post
    ){
        this.user = user;
        this.post = post;
    }
}
