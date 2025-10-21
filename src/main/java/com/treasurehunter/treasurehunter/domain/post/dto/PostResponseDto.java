package com.treasurehunter.treasurehunter.domain.post.dto;

import com.treasurehunter.treasurehunter.domain.post.domain.ItemCategory;
import com.treasurehunter.treasurehunter.domain.post.domain.Post;
import com.treasurehunter.treasurehunter.domain.post.domain.PostType;
import com.treasurehunter.treasurehunter.domain.post.domain.image.PostImage;
import com.treasurehunter.treasurehunter.domain.user.dto.UserSimpleResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
public class PostResponseDto {
    private final Long id;
    private final String title;
    private final String content;
    private final PostType type;
    private final UserSimpleResponseDto author;
    private final List<String> images;
    private final Integer setPoint;
    private final ItemCategory itemCategory;
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
        this.type = post.getType();
        this.author = new UserSimpleResponseDto(post.getAuthor());

        //게시물에 사진이 존재 할 때
        if(post.getImages() != null) {
            this.images = post.getImages().stream()
                    .sorted(Comparator.comparing(PostImage::getImageIndex))
                    .map(PostImage::getUrl)
                    .toList();
        } else {
            this.images = new ArrayList<>();
        }

        this.setPoint = post.getSetPoint();
        this.itemCategory = post.getItemCategory();
        this.lat = post.getLat();
        this.lon = post.getLon();
        this.lostAt = post.getLostAt();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.isAnonymous = post.isAnonymous();
        this.isCompleted = post.isCompleted();
    }
}
