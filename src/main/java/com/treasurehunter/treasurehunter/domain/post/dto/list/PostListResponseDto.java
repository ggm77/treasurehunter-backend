package com.treasurehunter.treasurehunter.domain.post.dto.list;

import com.treasurehunter.treasurehunter.domain.post.dto.PostListItemResponseDto;
import com.treasurehunter.treasurehunter.domain.post.entity.Post;
import com.treasurehunter.treasurehunter.domain.post.dto.PostSimpleResponseDto;
import lombok.Getter;

import java.util.List;

@Getter
public class PostListResponseDto {
    private final String clientMinLat;
    private final String clientMinLon;
    private final String clientMaxLat;
    private final String clientMaxLon;
    private final String clientLat;
    private final String clientLon;
    private final boolean hasNext;
    private final List<PostListItemResponseDto> posts;

    public PostListResponseDto(
            final List<PostListItemResponseDto> posts,
            final boolean hasNext,
            final String clientMinLat,
            final String clientMinLon,
            final String clientMaxLat,
            final String clientMaxLon,
            final String clientLat,
            final String clientLon
    ) {
        this.clientMinLat = clientMinLat;
        this.clientMinLon = clientMinLon;
        this.clientMaxLat = clientMaxLat;
        this.clientMaxLon = clientMaxLon;
        this.clientLat = clientLat;
        this.clientLon = clientLon;
        this.hasNext = hasNext;
        this.posts = posts;
    }
}
