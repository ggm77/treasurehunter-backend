package com.treasurehunter.treasurehunter.domain.post.service.list;

import com.treasurehunter.treasurehunter.domain.post.domain.Post;
import com.treasurehunter.treasurehunter.domain.post.dto.list.PostListResponseDto;
import com.treasurehunter.treasurehunter.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostListService {

    private final PostRepository postRepository;

    public PostListResponseDto getLatestPosts(){

        final List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();

        return new PostListResponseDto(posts);
    }
}
