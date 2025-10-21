package com.treasurehunter.treasurehunter.domain.post.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostRequestDto {

    @NotNull
    private String title;

    @NotNull
    private String content;

    @NotNull
    private String type;

    // 비어있어도 됨
    private List<String> images;

    @NotNull
    private Integer setPoint;

    @NotNull
    private String itemCategory;

    @NotNull
    private BigDecimal lat;

    @NotNull
    private BigDecimal lon;

    @NotNull
    private LocalDateTime lostAt;

    @NotNull
    private Boolean isAnonymous;
}
