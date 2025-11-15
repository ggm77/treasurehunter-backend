package com.treasurehunter.treasurehunter.domain.post.controller.like;

import com.treasurehunter.treasurehunter.domain.post.service.like.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    //게시글에 좋아요 표시하는 API
    @PostMapping("/post/{id}/like")
    public ResponseEntity<Void> likePost(
            @PathVariable("id") final String postId,
            @AuthenticationPrincipal String userIdStr
    ){
        final Long userId = Long.parseLong(userIdStr);
        postLikeService.likePost(Long.parseLong(postId), userId);

        return ResponseEntity.noContent().build();
    }

    //표시한 좋아요 취소하는 API
    @PostMapping("/post/{id}/unlike")
    public ResponseEntity<Void> unlikePost(
            @PathVariable("id") final String postId,
            @AuthenticationPrincipal String userIdStr
    ){
        final Long userId = Long.parseLong(userIdStr);

        postLikeService.unlikePost(Long.parseLong(postId), userId);

        return ResponseEntity.noContent().build();
    }
}
