package com.treasurehunter.treasurehunter.domain.chat.service;

import com.treasurehunter.treasurehunter.domain.chat.dto.ChatRequestDto;
import com.treasurehunter.treasurehunter.domain.chat.dto.ChatResponseDto;
import com.treasurehunter.treasurehunter.domain.chat.entity.Chat;
import com.treasurehunter.treasurehunter.domain.chat.repository.ChatRepository;
import com.treasurehunter.treasurehunter.domain.chat.repository.room.participant.ChatRoomParticipantRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatRepository chatRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;

    @Transactional
    public ChatResponseDto sendAndPushChat(
            final String userIdStr,
            final String roomId,
            final ChatRequestDto chatRequestDto
    ){

        // 1) 채팅방 참가중인 유저 아이디 가져오기
        final List<Long> participantIds =
                chatRoomParticipantRepository.findUserIdsByRoomId(roomId);

        // 2) 채팅방이 없거나 아무도 없는 경우 처리
        if(participantIds.size() != 2) {
            throw new CustomException(ExceptionCode.CHAT_ROOM_NOT_EXIST);
        }

        // 3) 채팅방 멤버가 아닌 경우 처리
        if(!participantIds.contains(Long.parseLong(userIdStr))){
            throw new CustomException(ExceptionCode.CHAT_ROOM_NOT_JOINED);
        }

        final List<Long> otherUserIds = participantIds.stream()
                .filter(id -> !id.equals(Long.parseLong(userIdStr)))
                .toList();

        final Long receiverId = otherUserIds.getFirst();

        // 4) 저장할 채팅 엔티티 생성
        final Chat chat = Chat.builder()
                .chatType(chatRequestDto.getType())
                .roomId(roomId)
                .senderId(userIdStr)
                .message(chatRequestDto.getMessage())
                .sendAt(chatRequestDto.getSentAt())
                .serverAt(LocalDateTime.now())
                .build();

        // 5) 채팅 저장
        final Chat savedChat = chatRepository.save(chat);

        // 6) 채팅 DTO에 담기
        final ChatResponseDto chatResponseDto = ChatResponseDto.builder()
                .chat(savedChat)
                .build();

        // 7) 메세지 헤더 설정
        final Map<String, Object> headers = Map.of(
                "persistent", "true", //메세지 내구성 설정 (ack 전까지 큐에 저장 되도록)
                "content-type", "application/json"
        );

        // 8) 채팅방에 채팅 전송
        //멀티 디바이스를 지원하게 된다면 주석 해제 해서 자신에게도 메세지 전송하도록 하기
//        simpMessagingTemplate.convertAndSend("/queue/chat.room."+roomId+".user."+userIdStr, chatResponseDto, headers);
        simpMessagingTemplate.convertAndSend("/queue/chat.room."+roomId+".user."+receiverId, chatResponseDto, headers);

        return chatResponseDto;
    }
}
