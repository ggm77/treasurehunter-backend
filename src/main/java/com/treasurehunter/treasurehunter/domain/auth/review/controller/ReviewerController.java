package com.treasurehunter.treasurehunter.domain.auth.review.controller;

import com.treasurehunter.treasurehunter.domain.auth.review.dto.ReviewerRequestDto;
import com.treasurehunter.treasurehunter.domain.auth.review.service.ReviewerService;
import com.treasurehunter.treasurehunter.domain.auth.token.dto.TokenResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewerController {

    private final ReviewerService reviewerService;

    @PostMapping("/auth/reviewer/login")
    public ResponseEntity<TokenResponseDto> reviewerLogin(
            @Validated @RequestBody final ReviewerRequestDto reviewerRequestDto
    ) {

        return ResponseEntity.ok().body(reviewerService.reviewerLogin(reviewerRequestDto));
    }
}
