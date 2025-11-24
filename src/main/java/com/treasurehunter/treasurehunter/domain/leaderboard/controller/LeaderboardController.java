package com.treasurehunter.treasurehunter.domain.leaderboard.controller;

import com.treasurehunter.treasurehunter.domain.leaderboard.dto.LeaderboardResponseDto;
import com.treasurehunter.treasurehunter.domain.leaderboard.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    //rankingType에 따라서 유저 순위 보여주는 API
    @GetMapping("/leaderboard")
    public ResponseEntity<LeaderboardResponseDto> getLeaderboard(
            @RequestParam(defaultValue = "points") final String rankingType
    ) {

        return ResponseEntity.ok().body(leaderboardService.getLeaderboard(rankingType));
    }
}
