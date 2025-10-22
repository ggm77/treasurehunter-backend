package com.treasurehunter.treasurehunter.domain.post.dto.list;

import com.treasurehunter.treasurehunter.domain.post.domain.Post;
import com.treasurehunter.treasurehunter.domain.post.dto.PostResponseDto;
import lombok.Getter;

import java.util.List;

@Getter
public class PostListResponseDto {
    private List<PostResponseDto> posts;

    public PostListResponseDto(final List<Post> posts) {
        this.posts = posts.stream()
                .map(PostResponseDto::new)
                .toList();
    }
}
