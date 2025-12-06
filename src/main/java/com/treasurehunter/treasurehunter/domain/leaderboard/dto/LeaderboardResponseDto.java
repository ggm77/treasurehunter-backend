package com.treasurehunter.treasurehunter.domain.leaderboard.dto;

import com.treasurehunter.treasurehunter.domain.user.dto.UserLeaderboardResponseDto;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class LeaderboardResponseDto {
    private final List<UserLeaderboardResponseDto> leaderboard;

    public LeaderboardResponseDto(
            final List<User> users,
            final Map<Long, Integer> foundCountMap
    ) {
        this.leaderboard = users.stream()
                .map(user -> new UserLeaderboardResponseDto(
                        user,
                        foundCountMap.getOrDefault(user.getId(), 0)
                ))
                .toList();
    }
}
