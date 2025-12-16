package com.treasurehunter.treasurehunter.domain.chat.service.sync;

import com.treasurehunter.treasurehunter.domain.chat.dto.list.ChatListResponseDto;
import com.treasurehunter.treasurehunter.domain.chat.entity.Chat;
import com.treasurehunter.treasurehunter.domain.chat.entity.read.ChatRead;
import com.treasurehunter.treasurehunter.domain.chat.repository.ChatRepository;
import com.treasurehunter.treasurehunter.domain.chat.repository.read.ChatReadRepository;
import com.treasurehunter.treasurehunter.domain.chat.repository.room.participant.ChatRoomParticipantRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatSyncService {

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

    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final ChatRepository chatRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ChatReadRepository chatReadRepository;

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

        // 4) 참가자 정보 조회
        final List<Tuple> participants =
                chatRoomParticipantRepository.findParticipantsAndIsCallerByRoomId(roomId);

        // 5) 참가자 정보 맵으로 추출
        final Map<Long, Boolean> participantsMap = participants.stream()
                .collect(Collectors.toMap(
                        t -> t.get("participantId", Long.class),
                        t -> t.get("isCaller", Boolean.class)
                ));

        // 6) 채팅방 멤버가 아닌 경우 처리
        if(!participantsMap.containsKey(userId)){
            throw new CustomException(ExceptionCode.CHAT_ROOM_NOT_JOINED);
        }

        // 7) 유저 타입(게시글 작성자, 채팅 건 사람) 확인
        final Boolean isCaller = participantsMap.get(userId);
        if(isCaller == null){
            throw new CustomException(ExceptionCode.USER_NOT_EXIST);
        }

        // 8) 마지막 채팅 id 기준으로 size+1개 만큼의 채팅 가져오기
        final List<Chat> storedChats =
                chatRepository.findByRoomIdAndIdGreaterThanOrderByIdAsc(
                        roomId,
                        lastChatId,
                        PageRequest.of(0, size+1)
                );

        // 9) size + 1의 값이 있는지 확인해서 채팅이 더 있는지 확인
        final boolean hasMore = storedChats.size() > size;

        // 10) 채팅이 더 존재한다면 size+1 만큼 들고온 리스트 size로 자르기
        final List<Chat> chats;
        if(hasMore){
            chats = storedChats.subList(0, size);
        } else {
            chats = storedChats;
        }

        // 11) 그 다음 채팅 기록을 가져올 수 있게하는 커서 값 저장
        final Long nextCursor;
        if(chats.isEmpty()) {
            nextCursor = lastChatId;
        } else {
            nextCursor = chats.get(chats.size()-1).getId();
        }

        // 12) 상대가 마지막으로 읽은 채팅 ID가져오기
        final String lKey = lastReadChatIdKey(roomId, !isCaller);
        final String lastReadChatIdStr = redisTemplate.opsForValue().get(lKey);
        final Long lastReadChatId;
        if(lastReadChatIdStr == null){
            final ChatRead chatRead = chatReadRepository.findByRoomIdAndIsCaller(roomId, !isCaller);

            //저장된게 없으면 null 반환
            if(chatRead != null) {
                lastReadChatId = chatRead.getLastReadChatId();
            } else {
                lastReadChatId = null;
            }
        } else {
            lastReadChatId = Long.parseLong(lastReadChatIdStr);
        }

        return new ChatListResponseDto(chats, nextCursor, hasMore, lastReadChatId);
    }
}
