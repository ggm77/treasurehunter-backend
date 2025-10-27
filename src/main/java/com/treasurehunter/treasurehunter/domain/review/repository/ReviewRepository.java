package com.treasurehunter.treasurehunter.domain.review.repository;

import com.treasurehunter.treasurehunter.domain.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
