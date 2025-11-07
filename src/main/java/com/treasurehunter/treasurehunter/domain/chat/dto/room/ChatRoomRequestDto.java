package com.treasurehunter.treasurehunter.domain.chat.dto.room;

import com.treasurehunter.treasurehunter.global.validation.Create;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ChatRoomRequestDto {

    @NotNull(groups = Create.class)
    private String name;

    @NotNull(groups = Create.class)
    private Long postId;

    @NotNull(groups = Create.class)
    private Boolean isAnonymous;
}
