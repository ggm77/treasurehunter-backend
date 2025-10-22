package com.treasurehunter.treasurehunter.domain.post.controller.list;

import com.treasurehunter.treasurehunter.domain.post.dto.list.PostListResponseDto;
import com.treasurehunter.treasurehunter.domain.post.service.list.PostListService;
import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostListController {

    private final JwtProvider jwtProvider;
    private final PostListService postListService;

    @GetMapping("/posts")
    public ResponseEntity<PostListResponseDto> getPosts(
            @RequestHeader(value = "Authorization") final String token
    ){

        jwtProvider.validateToken(token.substring(7));

        return ResponseEntity.ok(postListService.getLatestPosts());
    }
}
