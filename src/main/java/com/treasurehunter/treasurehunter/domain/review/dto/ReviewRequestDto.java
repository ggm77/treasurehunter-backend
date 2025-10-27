package com.treasurehunter.treasurehunter.domain.review.dto;

import com.treasurehunter.treasurehunter.global.validation.Create;
import com.treasurehunter.treasurehunter.global.validation.Update;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class ReviewRequestDto {

    @NotNull(groups = Create.class)
    private String title;

    @NotNull(groups = Create.class)
    private String content;

    @NotNull(groups = Create.class)
    @Min(value = 1, groups = { Create.class, Update.class })
    @Max(value = 5, groups = { Create.class, Update.class })
    private Integer score;

    @NotNull(groups = Create.class)
    private Long postId;

    //등록시 없어도 됨
    private List<String> images;
}
