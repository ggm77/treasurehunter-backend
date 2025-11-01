package com.treasurehunter.treasurehunter.domain.admin.badge.dto;

import com.treasurehunter.treasurehunter.domain.admin.badge.entity.Badge;
import lombok.Getter;

@Getter
public class BadgeResponseDto {
    private final Long id;
    private final String name;
    private final String description;

    public BadgeResponseDto(final Badge badge) {
        this.id = badge.getId();
        this.name = badge.getName();
        this.description = badge.getDescription();
    }
}
