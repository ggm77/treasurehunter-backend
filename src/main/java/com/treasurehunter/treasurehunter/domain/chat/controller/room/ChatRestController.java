package com.treasurehunter.treasurehunter.domain.chat.controller.room;

import com.treasurehunter.treasurehunter.domain.chat.dto.ChatRequestDto;
import com.treasurehunter.treasurehunter.domain.chat.dto.ChatResponseDto;
import com.treasurehunter.treasurehunter.domain.chat.service.ChatService;
import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import com.treasurehunter.treasurehunter.global.validation.Create;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatRestController {

    private final JwtProvider jwtProvider;
    private final ChatService chatService;

    //채팅 전송하는 API
    @PostMapping("/chat/room/{id}/send")
    public ResponseEntity<ChatResponseDto> sendChat(
            @PathVariable("id") final String roomId,
            @RequestHeader(value = "Authorization") final String token,
            @Validated(Create.class) @RequestBody final ChatRequestDto chatRequestDto
    ){
        final String userIdStr = jwtProvider.getPayload(token.substring(7));

        return ResponseEntity.ok().body(chatService.sendAndPushChat(userIdStr, roomId, chatRequestDto));
    }

}
