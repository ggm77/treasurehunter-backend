package com.treasurehunter.treasurehunter.domain.chat.controller;

import com.treasurehunter.treasurehunter.domain.chat.dto.ChatRequestDto;
import com.treasurehunter.treasurehunter.domain.chat.dto.ChatResponseDto;
import com.treasurehunter.treasurehunter.domain.chat.service.ChatService;
import com.treasurehunter.treasurehunter.global.validation.Create;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    //채팅 전송하는 API
    @PostMapping("/chat/room/{id}/messages")
    public ResponseEntity<ChatResponseDto> sendChat(
            @PathVariable("id") final String roomId,
            @AuthenticationPrincipal String userIdStr,
            @Validated(Create.class) @RequestBody final ChatRequestDto chatRequestDto
    ){

        return ResponseEntity.ok().body(chatService.sendAndPushChat(userIdStr, roomId, chatRequestDto));
    }

}
