package com.treasurehunter.treasurehunter.domain.chat.controller.room;

import com.treasurehunter.treasurehunter.domain.chat.dto.room.ChatRoomRequestDto;
import com.treasurehunter.treasurehunter.domain.chat.dto.room.ChatRoomResponseDto;
import com.treasurehunter.treasurehunter.domain.chat.dto.room.list.ChatRoomListResponseDto;
import com.treasurehunter.treasurehunter.domain.chat.service.room.ChatRoomService;
import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import com.treasurehunter.treasurehunter.global.validation.Create;
import com.treasurehunter.treasurehunter.global.validation.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ChatRoomController {

    private final JwtProvider jwtProvider;
    private final ChatRoomService chatRoomService;

    // 자신이 참가 중인 채팅방 리스트 조회하는 API
    @GetMapping("/chat/rooms")
    public ResponseEntity<ChatRoomListResponseDto> getChatRoomList(
            @RequestHeader(value = "Authorization") final String token
    ){
        final Long userId = Long.parseLong(jwtProvider.getPayload(token.substring(7)));

        return ResponseEntity.ok(chatRoomService.getChatRoomList(userId));
    }

    // 채팅방 생성하는 API
    @PostMapping("/chat/room")
    public ResponseEntity<ChatRoomResponseDto> createChatRoom(
            @RequestHeader(value = "Authorization") final String token,
            @Validated(Create.class) @RequestBody final ChatRoomRequestDto chatRoomRequestDto
    ){
        final Long userId = Long.parseLong(jwtProvider.getPayload(token.substring(7)));

        return ResponseEntity.ok(chatRoomService.createChatRoom(chatRoomRequestDto, userId));
    }

    // 참가 중인 채팅방 정보 조회하는 API
    @GetMapping("/chat/room/{id}")
    public ResponseEntity<ChatRoomResponseDto> getChatRoom(
            @PathVariable("id") final String chatRoomId,
            @RequestHeader(value = "Authorization") final String token
    ){
        final Long userId = Long.parseLong(jwtProvider.getPayload(token.substring(7)));

        return ResponseEntity.ok(chatRoomService.getChatRoom(chatRoomId, userId));
    }

    // 참가 중인 채팅방 정보 수정하는 API
    @PatchMapping("/chat/room/{id}")
    public ResponseEntity<ChatRoomResponseDto> updateChatRoom(
            @PathVariable("id") final String chatRoomId,
            @RequestHeader(value = "Authorization") final String token,
            @Validated(Update.class) @RequestBody final ChatRoomRequestDto chatRoomRequestDto
    ){
        final Long userId = Long.parseLong(jwtProvider.getPayload(token.substring(7)));

        return ResponseEntity.ok(chatRoomService.updateChatRoom(chatRoomId, chatRoomRequestDto, userId));
    }

    // 참가 중인 채팅방에서 나가는 API
    // 아무도 안남으면 채팅방 자동 삭제
    @DeleteMapping("/chat/room/{id}")
    public ResponseEntity<Void> deleteChatRoom(
            @PathVariable("id") final String chatRoomId,
            @RequestHeader(value = "Authorization") final String token
    ){
        final Long userId = Long.parseLong(jwtProvider.getPayload(token.substring(7)));

        chatRoomService.leaveChatRoom(chatRoomId, userId);

        return ResponseEntity.noContent().build();
    }
}
