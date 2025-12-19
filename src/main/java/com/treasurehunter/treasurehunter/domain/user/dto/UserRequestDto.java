package com.treasurehunter.treasurehunter.domain.user.dto;

import com.treasurehunter.treasurehunter.global.validation.Create;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UserRequestDto {

    @NotNull(groups = Create.class)
    private String nickname;

    @NotNull(groups = Create.class)
    private String profileImage;

    @NotNull(groups = Create.class)
    private String name;

    private String lat;
    private String lon;
}
