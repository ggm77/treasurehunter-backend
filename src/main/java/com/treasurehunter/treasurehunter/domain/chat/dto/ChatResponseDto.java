package com.treasurehunter.treasurehunter.domain.chat.dto;

import com.treasurehunter.treasurehunter.domain.chat.entity.Chat;
import com.treasurehunter.treasurehunter.domain.chat.entity.ChatType;
import com.treasurehunter.treasurehunter.domain.chat.entity.ChatUserType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatResponseDto {
    private final Long id;
    private final ChatType type;
    private final ChatUserType userType;
    private final String roomId;
    private final String message;
    private final LocalDateTime sentAt;
    private final LocalDateTime serverAt;

    @Builder
    public ChatResponseDto(final Chat chat){
        this.id = chat.getId();
        this.type = chat.getChatType();
        this.userType = chat.getUserType();
        this.roomId = chat.getRoomId();
        this.message = chat.getMessage();
        this.sentAt = chat.getSentAt();
        this.serverAt = chat.getServerAt();
    }
}
