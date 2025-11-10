package com.treasurehunter.treasurehunter.domain.chat.dto.error;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatErrorDto {
    private final LocalDateTime timestamp;
    private final String code;
    private final String message;
    private final String destination;

    @Builder
    public ChatErrorDto(
            final String code,
            final String message,
            final String destination
    ) {
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.destination = destination;
    }
}
