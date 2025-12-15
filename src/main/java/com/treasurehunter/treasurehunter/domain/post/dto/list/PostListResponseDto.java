package com.treasurehunter.treasurehunter.domain.post.dto.list;

import com.treasurehunter.treasurehunter.domain.post.entity.Post;
import com.treasurehunter.treasurehunter.domain.post.dto.PostSimpleResponseDto;
import lombok.Getter;

import java.util.List;

@Getter
public class PostListResponseDto {
    private boolean hasNext;
    private List<PostSimpleResponseDto> posts;

    public PostListResponseDto(
            final List<Post> posts,
            final boolean hasNext
    ) {
        this.hasNext = hasNext;
        this.posts = posts.stream()
                .map(PostSimpleResponseDto ::new)
                .toList();
    }
}
