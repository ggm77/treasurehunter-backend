package com.treasurehunter.treasurehunter.domain.leaderboard.dto;

import com.treasurehunter.treasurehunter.domain.user.dto.UserSimpleResponseDto;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import lombok.Getter;

import java.util.List;

@Getter
public class LeaderboardResponseDto {
    private final List<UserSimpleResponseDto> leaderboard;

    public LeaderboardResponseDto(final List<User> users) {
        this.leaderboard = users.stream()
                .map(UserSimpleResponseDto::new)
                .toList();
    }
}
