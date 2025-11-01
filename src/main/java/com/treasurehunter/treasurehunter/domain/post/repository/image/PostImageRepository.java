package com.treasurehunter.treasurehunter.domain.post.repository.image;

import com.treasurehunter.treasurehunter.domain.post.entity.image.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    void deleteByPostId(Long postId);
}
