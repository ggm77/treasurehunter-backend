package com.treasurehunter.treasurehunter.domain.chat.service;

import com.treasurehunter.treasurehunter.domain.chat.dto.ChatRequestDto;
import com.treasurehunter.treasurehunter.domain.chat.dto.ChatResponseDto;
import com.treasurehunter.treasurehunter.domain.chat.dto.list.ChatListResponseDto;
import com.treasurehunter.treasurehunter.domain.chat.entity.Chat;
import com.treasurehunter.treasurehunter.domain.chat.entity.ChatUserType;
import com.treasurehunter.treasurehunter.domain.chat.repository.ChatRepository;
import com.treasurehunter.treasurehunter.domain.chat.repository.room.participant.ChatRoomParticipantRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        // 1) 유저 아이디 파싱
        final Long userId = Long.parseLong(userIdStr);

        // 2) 채팅방 참가중인 유저 아이디와 유저가 채팅을 건 사람인지 여부 튜플로 가져오기
        final List<Tuple> participants =
                chatRoomParticipantRepository.findParticipantsAndIsCallerByRoomId(roomId);

        // 3) 참가자 정보 맵으로 추출
        final Map<Long, Boolean> participantsMap = participants.stream()
                .collect(Collectors.toMap(
                        t -> t.get("participantId", Long.class),
                        t -> t.get("isCaller", Boolean.class)
                ));

        log.info("Participant map: {}", participantsMap);

        // 4) 채팅방이 없거나 아무도 없는 경우 처리
        if(participantsMap.size() != 2) {
            throw new CustomException(ExceptionCode.CHAT_ROOM_NOT_EXIST);
        }

        // 5) 채팅방 멤버가 아닌 경우 처리
        if(!participantsMap.containsKey(userId)){
            throw new CustomException(ExceptionCode.CHAT_ROOM_NOT_JOINED);
        }

        // 6) 유저 타입(게시글 작성자, 채팅 건 사람) 지정
        final ChatUserType userType;
        if(Boolean.TRUE.equals(participantsMap.get(userId))) {
            userType = ChatUserType.CALLER;
        } else {
            userType = ChatUserType.AUTHOR;
        }

        // 7) 저장할 채팅 엔티티 생성
        final Chat chat = Chat.builder()
                .chatType(chatRequestDto.getType())
                .userType(userType)
                .roomId(roomId)
                .senderId(userIdStr)
                .message(chatRequestDto.getMessage())
                .sendAt(chatRequestDto.getSentAt())
                .serverAt(LocalDateTime.now())
                .build();

        // 8) 채팅 저장
        final Chat savedChat = chatRepository.save(chat);

        // 9) 채팅 DTO에 담기
        final ChatResponseDto chatResponseDto = ChatResponseDto.builder()
                .chat(savedChat)
                .build();

        // 10) 채팅방에 채팅 전송
        simpMessagingTemplate.convertAndSend("/topic/chat.room."+roomId, chatResponseDto);

        return chatResponseDto;
    }

    public ChatListResponseDto syncChat(
            final String roomId,
            final Long lastChatId,
            final String userIdStr,
            final int size
    ){
        // 1) size 값 검사
        if(size <= 0 || size > 300) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) 채팅 아이디 검사
        if(lastChatId == null) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 3) 유저 아이디 파싱
        final Long userId = Long.parseLong(userIdStr);

        // 4) 참가자인지 검사
        if(!chatRoomParticipantRepository.existsParticipantInChatRoom(roomId, userId)){
            throw new CustomException(ExceptionCode.CHAT_ROOM_NOT_JOINED);
        }

        // 5) 마지막 채팅 id 기준으로 size+1개 만큼의 채팅 가져오기
        final List<Chat> storedChats =
                chatRepository.findByRoomIdAndIdGreaterThanOrderByIdAsc(
                        roomId,
                        lastChatId,
                        PageRequest.of(0, size+1)
                );

        // 6) size + 1의 값이 있는지 확인해서 채팅이 더 있는지 확인
        final boolean hasMore = storedChats.size() > size;

        // 7) 채팅이 더 존재한다면 size+1 만큼 들고온 리스트 size로 자르기
        final List<Chat> chats;
        if(hasMore){
            chats = storedChats.subList(0, size);
        } else {
            chats = storedChats;
        }

        // 8) 그 다음 채팅 기록을 가져올 수 있게하는 커서 값 저장
        final Long nextCursor;
        if(chats.isEmpty()) {
            nextCursor = lastChatId;
        } else {
            nextCursor = chats.get(chats.size()-1).getId();
        }

        return new ChatListResponseDto(chats, nextCursor, hasMore);
    }
}
