package com.treasurehunter.treasurehunter.domain.post.repository;

import com.treasurehunter.treasurehunter.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
    Long countByAuthorId(Long userId);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + :delta WHERE p.id = :postId")
    int addViewCount(@Param("postId") Long postId, @Param("delta") Long delta);
}
