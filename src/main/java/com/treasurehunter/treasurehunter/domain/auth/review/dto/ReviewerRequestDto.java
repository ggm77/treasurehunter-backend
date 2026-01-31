package com.treasurehunter.treasurehunter.domain.auth.review.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReviewerRequestDto {
    @NotNull
    private String id;
    @NotNull
    private String password;
}
