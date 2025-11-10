package com.treasurehunter.treasurehunter.domain.chat.service;

import com.treasurehunter.treasurehunter.domain.chat.dto.ChatRequestDto;
import com.treasurehunter.treasurehunter.domain.chat.dto.ChatResponseDto;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatService {

    /**
     * 보낼 채팅 검증 및 처리하는 메서드
     * (저장은 추후 구현 예정)
     * @param roomId 채팅 보낼 채팅방 ID
     * @param chatRequestDto 채팅 정보 담긴 DTO
     * @param principal 채팅 보낸 사람 정보
     * @param sessionId 채팅 보낸 사람 세션 ID
     * @return 보낼 채팅 DTO
     */
    public ChatResponseDto saveMessage(
            final String roomId,
            final ChatRequestDto chatRequestDto,
            final Principal principal,
            final String sessionId
    ){
        // 0) null / blank검사 DTO에서 진행

        // 1) 입력 값 검증
        if(!roomId.equals(chatRequestDto.getRoomId())){
            throw new CustomException(ExceptionCode.CHAT_ROOM_ID_NOT_MATCH);
        }

        // 2) 저장 및 전송할 메세지 DTO 생성
        final ChatResponseDto chat = ChatResponseDto.builder()
                .chatRequestDto(chatRequestDto)
                .serverAt(LocalDateTime.now())
                .build();

        // 3) 메세지 저장
        // 추후 구현

        return chat;
    }
}
