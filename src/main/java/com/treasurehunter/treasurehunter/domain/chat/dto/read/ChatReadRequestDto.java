package com.treasurehunter.treasurehunter.domain.chat.dto.read;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ChatReadRequestDto {

    @NotNull
    private Long lastReadChatId;
}
