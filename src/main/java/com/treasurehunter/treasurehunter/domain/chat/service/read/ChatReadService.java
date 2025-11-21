package com.treasurehunter.treasurehunter.domain.chat.service.read;

import com.treasurehunter.treasurehunter.domain.chat.dto.read.ChatReadRequestDto;
import com.treasurehunter.treasurehunter.domain.chat.dto.read.ChatReadResponseDto;
import com.treasurehunter.treasurehunter.domain.chat.entity.ChatUserType;
import com.treasurehunter.treasurehunter.domain.chat.repository.read.ChatReadRepository;
import com.treasurehunter.treasurehunter.domain.chat.repository.room.participant.ChatRoomParticipantRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatReadService {

    private static String lastReadChatIdKey(
            final String roomId,
            final boolean isCaller
    ){
        if(isCaller){
            return "chat.read.lastReadChatId:"+roomId+":CALLER";
        } else {
            return "chat.read.lastReadChatId:"+roomId+":AUTHOR";
        }

    }

    private static String rdbSavedAtKey(
            final String roomId,
            final boolean isCaller
    ){
        if(isCaller){
            return "chat.read.rdbSavedAt:"+roomId+":CALLER";
        } else {
            return "chat.read.rdbSavedAt:"+roomId+":AUTHOR";
        }

    }

    private final RedisTemplate<String, String> redisTemplate;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final ChatReadRepository chatReadRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     * 채팅 읽었다고 표시하는 메서드
     * 유저가 어느 채팅까지 읽었는지 redis와 rdb에 저장한다.
     * rdb에는 마지막 저장 시점으로 부터 5분 지나있으면 저장함.
     * @param roomId 입장한 채팅방 아이디
     * @param chatReadRequestDto 메서드 처리하기 위한 정보가 들어있는 DTO
     * @param userIdStr 요청한 유저 아이디의 문자열
     * @return 어느 채팅까지 읽었는지, 읽은 유저의 타입이 뭔지 저장된 DTO
     */
    @Transactional
    public ChatReadResponseDto updateReadCursor(
            final String roomId,
            final ChatReadRequestDto chatReadRequestDto,
            final String userIdStr
    ){
        // 1) lastReadChatId 검증 및 변수 지정
        final Long lastReadChatId = chatReadRequestDto.getLastReadChatId();
        // null 검사 및 0보다 작은 경우 예외처리
        if(lastReadChatId == null || lastReadChatId <= 0){
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) 현재 시각 변수 지정
        final LocalDateTime now = LocalDateTime.now();
        final String rdbSavedAtStr = now.toString();

        // 3) 유저 아이디 변환
        final Long userId = Long.parseLong(userIdStr);

        // 4) 참가자 정보 조회
        final List<Tuple> participants =
                chatRoomParticipantRepository.findParticipantsAndIsCallerByRoomId(roomId);

        // 5) 참가자 정보 맵으로 추출
        final Map<Long, Boolean> participantsMap = participants.stream()
                .collect(Collectors.toMap(
                        t -> t.get("participantId", Long.class),
                        t -> t.get("isCaller", Boolean.class)
                ));

        // 6) 채팅방이 없거나 아무도 없는 경우 처리
        if(participantsMap.size() != 2) {
            throw new CustomException(ExceptionCode.CHAT_ROOM_NOT_EXIST);
        }

        // 7) 채팅방 멤버가 아닌 경우 처리
        if(!participantsMap.containsKey(userId)){
            throw new CustomException(ExceptionCode.CHAT_ROOM_NOT_JOINED);
        }

        // 8) 유저 타입(게시글 작성자, 채팅 건 사람) 확인
        final Boolean isCaller = participantsMap.get(userId);
        if(isCaller == null){
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }
        final ChatUserType userType;
        if(isCaller) {
            userType = ChatUserType.CALLER;
        } else {
            userType = ChatUserType.AUTHOR;
        }

        // 9) redis key 생성
        final String lKey = lastReadChatIdKey(roomId, isCaller);
        final String rKey = rdbSavedAtKey(roomId, isCaller);

        // 10) 이전 rdbSavedAt 값 가져오기
        final String previousRdbSavedAt = redisTemplate.opsForValue().get(rKey);

        // 11) redis에 저장
        redisTemplate.opsForValue().set(lKey, String.valueOf(lastReadChatId));

        // 12) rdb에 저장해야하는지 판단 (이전 저장으로부터 5분 지나면 저장)
        final boolean shouldSaveToRdb;
        if(previousRdbSavedAt == null){
            shouldSaveToRdb = true;
        } else {
            final LocalDateTime previousAt = LocalDateTime.parse(previousRdbSavedAt);
            shouldSaveToRdb = previousAt.plusMinutes(5).isBefore(now);
        }

        // 13) rdb 저장
        if(shouldSaveToRdb){
            // upsert로 없으면 저장, 있으면 수정
            chatReadRepository.upsertChatRead(lastReadChatId, roomId, isCaller);

            // redis에 저장 시점 저장
            redisTemplate.opsForValue().set(rKey, rdbSavedAtStr);
        }

        // 14) response dto 생성
        final ChatReadResponseDto chatReadResponseDto = ChatReadResponseDto.builder()
                .lastReadChatId(lastReadChatId)
                .userType(userType)
                .build();

        // 15) 읽었다고 전송
        simpMessagingTemplate.convertAndSend("/topic/chat.room."+roomId+".read", chatReadResponseDto);

        return chatReadResponseDto;
    }
}
