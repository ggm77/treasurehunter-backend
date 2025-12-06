package com.treasurehunter.treasurehunter.domain.post.controller.list;

import com.treasurehunter.treasurehunter.domain.post.dto.list.PostListResponseDto;
import com.treasurehunter.treasurehunter.domain.post.service.list.PostListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostListController {

    private final PostListService postListService;

    @GetMapping("/posts")
    public ResponseEntity<PostListResponseDto> getPosts(
            @RequestParam(required = false) final String postType
    ){

        return ResponseEntity.ok(postListService.getLatestPosts(postType));
    }
}
