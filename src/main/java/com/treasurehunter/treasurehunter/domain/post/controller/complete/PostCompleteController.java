package com.treasurehunter.treasurehunter.domain.post.controller.complete;

import com.treasurehunter.treasurehunter.domain.post.service.complete.PostCompleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostCompleteController {

    private final PostCompleteService postCompleteService;

    @PostMapping("/post/{id}/complete")
    public ResponseEntity<Void> completePost(
            @PathVariable("id") final Long postId,
            @AuthenticationPrincipal final String userIdStr
    ){
        final Long requestUserId = Long.parseLong(userIdStr);

        postCompleteService.completePost(requestUserId, postId);

        return ResponseEntity.noContent().build();
    }
}
