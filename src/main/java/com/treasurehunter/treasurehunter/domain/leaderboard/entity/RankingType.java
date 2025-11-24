package com.treasurehunter.treasurehunter.domain.leaderboard.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RankingType {
    POINTS("points", "보유 포인트"),
    RETURNS("returns", "물건 찾아준 횟수"),
    FINDS("finds", "물건 발견 횟수"),
    ;

    private final String key;
    private final String description;
}
