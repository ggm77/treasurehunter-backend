package com.treasurehunter.treasurehunter.domain.post.controller;

import com.treasurehunter.treasurehunter.domain.post.dto.PostRequestDto;
import com.treasurehunter.treasurehunter.domain.post.dto.PostResponseDto;
import com.treasurehunter.treasurehunter.domain.post.service.PostService;
import com.treasurehunter.treasurehunter.global.validation.Create;
import com.treasurehunter.treasurehunter.global.validation.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PostController {

    private final PostService postService;

    @PostMapping("/post")
    public ResponseEntity<PostResponseDto> createPost(
            @AuthenticationPrincipal String userIdStr,
            @Validated(Create.class) @RequestBody final PostRequestDto postRequestDto
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(postService.createPost(postRequestDto, userId));
    }

    @GetMapping("/post/{id}")
    public ResponseEntity<PostResponseDto> getPost(
            @PathVariable final Long id
    ){

        return ResponseEntity.ok(postService.getPost(id));
    }

    @PatchMapping("/post/{id}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable final Long id,
            @AuthenticationPrincipal String userIdStr,
            @Validated(Update.class) @RequestBody final PostRequestDto postRequestDto
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(postService.updatePost(id, postRequestDto, userId));
    }

    @DeleteMapping("/post/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable final Long id,
            @AuthenticationPrincipal String userIdStr
    ){

        final Long userId = Long.parseLong(userIdStr);

        postService.deletePost(id, userId);

        return ResponseEntity.noContent().build();
    }
}
