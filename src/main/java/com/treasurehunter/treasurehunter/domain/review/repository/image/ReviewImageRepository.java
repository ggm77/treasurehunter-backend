package com.treasurehunter.treasurehunter.domain.review.repository.image;

import com.treasurehunter.treasurehunter.domain.review.domain.image.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    void deleteByReviewId(Long reviewId);
}
