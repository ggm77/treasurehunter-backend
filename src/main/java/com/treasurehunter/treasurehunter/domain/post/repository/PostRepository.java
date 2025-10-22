package com.treasurehunter.treasurehunter.domain.post.repository;

import com.treasurehunter.treasurehunter.domain.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
}
