package com.treasurehunter.treasurehunter.domain.post.dto;

import com.treasurehunter.treasurehunter.domain.post.domain.Post;
import com.treasurehunter.treasurehunter.domain.user.dto.UserSimpleResponseDto;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostResponseDto {
    private final Long id;
    private final String title;
    private final String content;
    private final String type;
    private final UserSimpleResponseDto author;
    private final List<String> images;
    private final Integer setPoint;
    private final String itemCategory;
    private final BigDecimal lat;
    private final BigDecimal lon;
    private final LocalDateTime lostAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final Boolean isAnonymous;
    private final Boolean isCompleted;
    //리뷰 추가

    public PostResponseDto(final Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.type = post.getType().name();

        //익명인 경우 유저 정보 제공 X
        if(!post.isAnonymous()) {
            this.author = new UserSimpleResponseDto(post.getAuthor());
        } else {
            this.author = null;
        }

        this.images = post.getImagesUrls();
        this.setPoint = post.getSetPoint();
        this.itemCategory = post.getItemCategory().name();
        this.lat = post.getLat();
        this.lon = post.getLon();
        this.lostAt = post.getLostAt();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.isAnonymous = post.isAnonymous();
        this.isCompleted = post.isCompleted();
    }
}
