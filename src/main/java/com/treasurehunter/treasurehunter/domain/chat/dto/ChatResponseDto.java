package com.treasurehunter.treasurehunter.domain.chat.dto;

import com.treasurehunter.treasurehunter.domain.chat.entity.ChatType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatResponseDto {
    private final ChatType type;
    private final String roomId;
    private final String sender;
    private final String message;
    private final LocalDateTime sentAt;
    private final LocalDateTime serverAt;

    @Builder
    public ChatResponseDto(
            final ChatRequestDto chatRequestDto,
            final LocalDateTime serverAt
    ){
        this.type = chatRequestDto.getType();
        this.roomId = chatRequestDto.getRoomId();
        this.sender = chatRequestDto.getSender();
        this.message = chatRequestDto.getMessage();
        this.sentAt = chatRequestDto.getSentAt();
        this.serverAt = serverAt;
    }
}
