package com.treasurehunter.treasurehunter.domain.review.entity.image;

import com.treasurehunter.treasurehunter.domain.review.entity.Review;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2048, nullable = false)
    private String url;

    @Column(nullable = false)
    private int imageIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @Builder
    public ReviewImage(
            final String url,
            final int imageIndex
    ){
        this.url = url;
        this.imageIndex = imageIndex;
    }

    public void updateReview(final Review review){
        this.review = review;
    }
}
