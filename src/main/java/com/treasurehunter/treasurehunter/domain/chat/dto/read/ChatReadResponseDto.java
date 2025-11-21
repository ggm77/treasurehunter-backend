package com.treasurehunter.treasurehunter.domain.chat.dto.read;

import com.treasurehunter.treasurehunter.domain.chat.entity.ChatUserType;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ChatReadResponseDto {

    private final Long lastReadChatId;
    private final ChatUserType userType;

    @Builder
    public ChatReadResponseDto(
            final Long lastReadChatId,
            final ChatUserType userType
    ){
        this.lastReadChatId = lastReadChatId;
        this.userType = userType;
    }
}
