package com.treasurehunter.treasurehunter.domain.chat.dto;

import com.treasurehunter.treasurehunter.domain.chat.entity.ChatType;
import lombok.Getter;

@Getter
public class ChatDto {

    private ChatType type;
    private String roomId;
    private String sender;
    private String message;

}
