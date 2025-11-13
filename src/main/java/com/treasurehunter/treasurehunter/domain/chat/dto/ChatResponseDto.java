package com.treasurehunter.treasurehunter.domain.chat.dto;

import com.treasurehunter.treasurehunter.domain.chat.entity.Chat;
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
    public ChatResponseDto(final Chat chat){
        this.type = chat.getChatType();
        this.roomId = chat.getRoomId();
        this.sender = chat.getSenderId();
        this.message = chat.getMessage();
        this.sentAt = chat.getSentAt();
        this.serverAt = chat.getServerAt();
    }
}
