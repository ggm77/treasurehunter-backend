package com.treasurehunter.treasurehunter.domain.post.controller;

import com.treasurehunter.treasurehunter.domain.post.dto.PostRequestDto;
import com.treasurehunter.treasurehunter.domain.post.dto.PostResponseDto;
import com.treasurehunter.treasurehunter.domain.post.service.PostService;
import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PostController {

    private final JwtProvider jwtProvider;
    private final PostService postService;

    @PostMapping("/post")
    public ResponseEntity<PostResponseDto> createPost(
            @RequestHeader(value = "Authorization") final String token,
            @Valid @RequestBody final PostRequestDto postRequestDto
    ){

        final Long userId = Long.parseLong(jwtProvider.validateToken(token.substring(7)));

        final PostResponseDto postResponseDto = postService.createPost(postRequestDto, userId);

        return ResponseEntity.ok(postResponseDto);
    }
}
