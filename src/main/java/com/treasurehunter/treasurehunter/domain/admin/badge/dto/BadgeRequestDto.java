package com.treasurehunter.treasurehunter.domain.admin.badge.dto;

import com.treasurehunter.treasurehunter.global.validation.Create;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class BadgeRequestDto {

    @NotNull(groups = Create.class)
    private String name;

    @NotNull(groups = Create.class)
    private String description;
}
