package com.treasurehunter.treasurehunter.domain.chat.dto;

import com.treasurehunter.treasurehunter.domain.chat.entity.ChatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatRequestDto {

    @NotNull
    private ChatType type;

    @NotBlank
    private String roomId;

    @NotBlank
    private String message;

    // UTC+0로 보내는걸 권장
    @NotNull
    private LocalDateTime sentAt;

    @NotBlank
    private String nickname;

    @NotBlank
    private String profileImage;
}
