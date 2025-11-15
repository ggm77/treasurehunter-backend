package com.treasurehunter.treasurehunter.domain.userBadge.controller.list;

import com.treasurehunter.treasurehunter.domain.userBadge.dto.list.UserBadgeListResponseDto;
import com.treasurehunter.treasurehunter.domain.userBadge.service.list.UserBadgeListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserBadgeListController {

    private final UserBadgeListService userBadgeListService;

    //유저가 가진 뱃지 정보 리스트를 주는 API
    @GetMapping("/user/{id}/badges")
    public ResponseEntity<UserBadgeListResponseDto> getUserBadges(
            @PathVariable("id") final Long targetUserId
    ){

        return ResponseEntity.ok(userBadgeListService.getUserBadges(targetUserId));
    }
}
