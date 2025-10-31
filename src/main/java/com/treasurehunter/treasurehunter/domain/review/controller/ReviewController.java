package com.treasurehunter.treasurehunter.domain.review.controller;

import com.treasurehunter.treasurehunter.domain.review.dto.ReviewRequestDto;
import com.treasurehunter.treasurehunter.domain.review.dto.ReviewResponseDto;
import com.treasurehunter.treasurehunter.domain.review.service.ReviewService;
import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import com.treasurehunter.treasurehunter.global.validation.Create;
import com.treasurehunter.treasurehunter.global.validation.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final JwtProvider jwtProvider;
    private final ReviewService reviewService;

    //후기 등록 API
    @PostMapping("/review")
    public ResponseEntity<ReviewResponseDto> createReview(
            @RequestHeader(value = "Authorization") final String token,
            @Validated(Create.class) @RequestBody final ReviewRequestDto reviewRequestDto
    ){
        final Long userId = Long.parseLong(jwtProvider.getPayload(token.substring(7)));

        return ResponseEntity.ok().body(reviewService.createReview(reviewRequestDto, userId));
    }

    //후기 조회 API
    @GetMapping("/review/{id}")
    public ResponseEntity<ReviewResponseDto> getReview(
            @PathVariable("id") final Long reviewId,
            @RequestHeader(value = "Authorization") final String token
    ){
        jwtProvider.getPayload(token.substring(7));

        return ResponseEntity.ok().body(reviewService.getReview(reviewId));
    }

    //후기 수정 API
    @PatchMapping("/review/{id}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable("id") final Long reviewId,
            @RequestHeader(value = "Authorization") final String token,
            @Validated(Update.class) @RequestBody final ReviewRequestDto reviewRequestDto
    ){
        final Long userId = Long.parseLong(jwtProvider.getPayload(token.substring(7)));

        return ResponseEntity.ok().body(reviewService.updateReview(reviewRequestDto, reviewId, userId));
    }

    //후기 삭제 API
    @DeleteMapping("/review/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable("id") final Long reviewId,
            @RequestHeader(value = "Authorization") final String token
    ){
        final Long userId = Long.parseLong(jwtProvider.getPayload(token.substring(7)));

        reviewService.deleteReview(reviewId, userId);

        return ResponseEntity.noContent().build();
    }
}
