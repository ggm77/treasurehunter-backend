package com.treasurehunter.treasurehunter.domain.notification.controller.token;

import com.treasurehunter.treasurehunter.domain.notification.dto.token.NotificationTokenRequestDto;
import com.treasurehunter.treasurehunter.domain.notification.service.token.NotificationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class NotificationTokenController {

    private final NotificationTokenService notificationTokenService;

    //FCM 토큰을 upsert 하는 API
    @PostMapping("/notification/token")
    public ResponseEntity<Void> upsertNotificationToken(
            @AuthenticationPrincipal final String userIdStr,
            @RequestBody final NotificationTokenRequestDto notificationTokenRequestDto
    ){

        notificationTokenService.upsertNotificationToken(userIdStr, notificationTokenRequestDto);

        return ResponseEntity.noContent().build();
    }

    //FCM 토큰 삭제하는 API
    @DeleteMapping("/notification/token")
    public ResponseEntity<Void> deleteNotificationToken(
            @AuthenticationPrincipal final String userIdStr,
            @RequestParam final String platform
    ){

        notificationTokenService.deleteNotificationToken(userIdStr, platform);

        return ResponseEntity.noContent().build();
    }
}
