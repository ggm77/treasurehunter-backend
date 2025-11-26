package com.treasurehunter.treasurehunter.domain.review.repository;

import com.treasurehunter.treasurehunter.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Long countByAuthorId(Long userId);
    boolean existsByAuthorIdAndTargetUserId(Long authorId, Long targetUserId);
}
