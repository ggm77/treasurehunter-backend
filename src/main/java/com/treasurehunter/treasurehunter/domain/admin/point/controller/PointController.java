package com.treasurehunter.treasurehunter.domain.admin.point.controller;

import com.treasurehunter.treasurehunter.domain.admin.point.dto.PointRequestDto;
import com.treasurehunter.treasurehunter.domain.admin.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    //포인트 증가용 API
    @PostMapping("/admin/user/{id}/point/increase")
    public ResponseEntity<Void> increaseUserPoint(
            @PathVariable("id") final Long targetUserId,
            @AuthenticationPrincipal final String userIdStr,
            @Validated @RequestBody final PointRequestDto pointRequestDto
    ){
        final Long userId = Long.parseLong(userIdStr);

        pointService.updateUserPoint(userId, targetUserId, pointRequestDto, true);

        return ResponseEntity.noContent().build();
    }

    //포인트 감소용 API
    @PostMapping("/admin/user/{id}/point/decrease")
    public ResponseEntity<Void> decreaseUserPoint(
            @PathVariable("id") final Long targetUserId,
            @AuthenticationPrincipal final String userIdStr,
            @Validated @RequestBody final PointRequestDto pointRequestDto
    ){
        final Long userId = Long.parseLong(userIdStr);

        pointService.updateUserPoint(userId, targetUserId, pointRequestDto, false);

        return ResponseEntity.noContent().build();
    }
}
