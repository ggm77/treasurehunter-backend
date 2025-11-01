package com.treasurehunter.treasurehunter.domain.review.dto;

import com.treasurehunter.treasurehunter.domain.review.entity.Review;
import com.treasurehunter.treasurehunter.domain.user.dto.UserSimpleResponseDto;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ReviewSimpleResponseDto {

    private final Long id;
    private final UserSimpleResponseDto author;
    private final String title;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String content;
    private final Integer score;
    private final List<String> images;

    public ReviewSimpleResponseDto(final Review review){
        this.id = review.getId();
        if(review.getAuthor() != null) {
            this.author = new UserSimpleResponseDto(review.getAuthor());
        } else {
            this.author = null;
        }
        this.title = review.getTitle();
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
        this.content = review.getContent();
        this.score = review.getScore();
        this.images = review.getImageUrls();

    }
}
