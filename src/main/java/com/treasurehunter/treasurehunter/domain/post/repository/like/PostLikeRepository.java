package com.treasurehunter.treasurehunter.domain.post.repository.like;

import com.treasurehunter.treasurehunter.domain.post.entity.like.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<Boolean> existsByPostIdAndUserId(Long postId, Long userId);
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);
    void deleteByPostId(Long postId);
}
