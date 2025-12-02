package com.treasurehunter.treasurehunter.domain.admin.point.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PointRequestDto {

    @NotNull
    private Integer amount;
}
