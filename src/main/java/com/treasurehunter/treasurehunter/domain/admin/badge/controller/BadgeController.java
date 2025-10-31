package com.treasurehunter.treasurehunter.domain.admin.badge.controller;

import com.treasurehunter.treasurehunter.domain.admin.badge.dto.BadgeRequestDto;
import com.treasurehunter.treasurehunter.domain.admin.badge.dto.BadgeResponseDto;
import com.treasurehunter.treasurehunter.domain.admin.badge.service.BadgeService;
import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import com.treasurehunter.treasurehunter.global.validation.Create;
import com.treasurehunter.treasurehunter.global.validation.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BadgeController {

    private final JwtProvider jwtProvider;
    private final BadgeService badgeService;

    //뱃지 추가하는 API
    @PostMapping("/admin/badge")
    public ResponseEntity<BadgeResponseDto> createBadge(
            @RequestHeader(value = "Authorization") final String token,
            @Validated(Create.class) @RequestBody final BadgeRequestDto badgeRequestDto
    ){
        final Long userId = Long.parseLong(jwtProvider.getPayload(token.substring(7)));

        return ResponseEntity.ok(badgeService.createBadge(badgeRequestDto, userId));
    }

    //뱃지 조회하는 API
    @GetMapping("/admin/badge/{id}")
    public ResponseEntity<BadgeResponseDto> getBadge(
            @PathVariable("id") final Long badgeId,
            @RequestHeader(value = "Authorization") final String token
    ){
        jwtProvider.getPayload(token.substring(7));

        return ResponseEntity.ok(badgeService.getBadge(badgeId));
    }

    //뱃지 수정하는 API
    @PatchMapping("/admin/badge/{id}")
    public ResponseEntity<BadgeResponseDto> updateBadge(
            @PathVariable("id") final Long badgeId,
            @RequestHeader(value = "Authorization") final String token,
            @Validated(Update.class) @RequestBody final BadgeRequestDto badgeRequestDto
    ){
        final Long userId = Long.parseLong(jwtProvider.getPayload(token.substring(7)));

        return ResponseEntity.ok(badgeService.updateBadge(badgeId, badgeRequestDto, userId));
    }

    //뱃지 삭제하는 API
    @DeleteMapping("/admin/badge/{id}")
    public ResponseEntity<BadgeResponseDto> deleteBadge(
            @PathVariable("id") final Long badgeId,
            @RequestHeader(value = "Authorization") final String token
    ){
        final Long userId = Long.parseLong(jwtProvider.getPayload(token.substring(7)));

        badgeService.deleteBadge(badgeId, userId);

        return ResponseEntity.noContent().build();
    }
}
