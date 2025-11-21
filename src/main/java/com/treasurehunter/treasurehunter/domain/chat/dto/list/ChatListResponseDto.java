package com.treasurehunter.treasurehunter.domain.chat.dto.list;

import com.treasurehunter.treasurehunter.domain.chat.dto.ChatResponseDto;
import com.treasurehunter.treasurehunter.domain.chat.entity.Chat;
import lombok.Getter;

import java.util.List;

@Getter
public class ChatListResponseDto {
    private final List<ChatResponseDto> chats;
    private final Long nextCursor;
    private final boolean hasMore;

    //상대가 마지막으로 읽은 채팅 ID
    private final Long opponentLastReadChatId;

    public ChatListResponseDto(
            final List<Chat> chats,
            final Long nextCursor,
            final boolean hasMore,
            final Long opponentLastReadChatId
    ) {
        this.chats = chats.stream()
                .map(ChatResponseDto::new)
                .toList();
        this.nextCursor = nextCursor;
        this.hasMore = hasMore;
        this.opponentLastReadChatId = opponentLastReadChatId;
    }
}
