package com.treasurehunter.treasurehunter.domain.post.dto;

import com.treasurehunter.treasurehunter.domain.post.entity.Post;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostSimpleResponseDto {

    private final Long id;
    private final String title;
    private final String content;
    private final String type;
    private final List<String> images;
    private final Integer setPoint;
    private final String itemCategory;
    private final BigDecimal lat;
    private final BigDecimal lon;
    private final LocalDateTime lostAt;
    private final Long likeCount;
    private final Long viewCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final Boolean isAnonymous;
    private final Boolean isCompleted;

    public PostSimpleResponseDto(final Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.type = post.getType().name();
        this.images = post.getImagesUrls();
        this.setPoint = post.getSetPoint();
        this.itemCategory = post.getItemCategory().name();
        this.lat = post.getLat();
        this.lon = post.getLon();
        this.lostAt = post.getLostAt();
        this.likeCount = post.getLikeCount();
        this.viewCount = post.getViewCount();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.isAnonymous = post.isAnonymous();
        this.isCompleted = post.isCompleted();
    }
}
