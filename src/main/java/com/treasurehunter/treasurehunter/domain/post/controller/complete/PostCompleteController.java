package com.treasurehunter.treasurehunter.domain.post.controller.complete;

import com.treasurehunter.treasurehunter.domain.post.service.complete.PostCompleteService;
import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostCompleteController {

    private final JwtProvider jwtProvider;
    private final PostCompleteService postCompleteService;

    @PostMapping("/post/{id}/complete")
    public ResponseEntity<Void> completePost(
            @PathVariable("id") final Long postId,
            @RequestHeader(value = "Authorization") final String token
    ){
        final Long requestUserId = Long.parseLong(jwtProvider.getPayload(token.substring(7)));

        postCompleteService.completePost(requestUserId, postId);

        return ResponseEntity.noContent().build();
    }
}
