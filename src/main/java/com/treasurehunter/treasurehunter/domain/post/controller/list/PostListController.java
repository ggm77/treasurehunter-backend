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

    //게시물 리스트를 조회하는 API
    @GetMapping("/posts")
    public ResponseEntity<PostListResponseDto> getPosts(
            @RequestParam(required = false) final String searchType,
            @RequestParam(required = false) final String query,
            @RequestParam(required = false) final String minLat,
            @RequestParam(required = false) final String minLon,
            @RequestParam(required = false) final String maxLat,
            @RequestParam(required = false) final String maxLon,
            @RequestParam(required = false) final String postType,
            @RequestParam(required = false, defaultValue = "10") final Integer size,
            @RequestParam(required = false, defaultValue = "0") final Integer page
    ){

        return ResponseEntity.ok(postListService.searchPosts(
                searchType,
                query,
                minLat,
                minLon,
                maxLat,
                maxLon,
                postType,
                size,
                page
        ));
    }
}
