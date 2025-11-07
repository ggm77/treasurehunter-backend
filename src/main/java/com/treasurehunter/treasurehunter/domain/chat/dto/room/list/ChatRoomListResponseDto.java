package com.treasurehunter.treasurehunter.domain.chat.dto.room.list;

import com.treasurehunter.treasurehunter.domain.chat.dto.room.ChatRoomResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ChatRoomListResponseDto {
    private final List<ChatRoomResponseDto> chatRooms;

    @Builder
    public ChatRoomListResponseDto(final List<ChatRoomResponseDto> chatRooms) {
        this.chatRooms = chatRooms;
    }
}
