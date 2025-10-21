package com.treasurehunter.treasurehunter.domain.post.domain.image;

import com.treasurehunter.treasurehunter.domain.post.domain.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String url;

    @Column(nullable = false)
    private int imageIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Builder
    public PostImage(final String url, final int imageIndex, final Post post) {
        this.url = url;
        this.imageIndex = imageIndex;
        this.post = post;
    }
}
