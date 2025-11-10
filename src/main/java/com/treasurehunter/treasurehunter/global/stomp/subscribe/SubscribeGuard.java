package com.treasurehunter.treasurehunter.global.stomp.subscribe;

import com.treasurehunter.treasurehunter.domain.chat.service.room.ChatRoomService;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import com.treasurehunter.treasurehunter.global.stomp.constants.StompConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class SubscribeGuard {

    private final ChatRoomService chatRoomService;

    /**
     * 유저가 요청한 채팅방에 구독(입장)이 가능함을 보장하는 메서드
     * @param accessor 검사를 진행할 STOMP 메세지의 헤더 정보를 포함한 객체
     */
    public void assertCanSubscribeChatRoom(
            final StompHeaderAccessor accessor,
            final String destination
    ) {
        // 1) accessor에서 Principal 추출
        final Principal principal = accessor.getUser();

        // 2) 인증 안된(== Principal 없음) 경우 예외 처리
        if(principal == null) {
            throw new CustomException(ExceptionCode.AUTHENTICATION_ERROR);
        }

        // 3) Principal에서 정보 추출
        final String userIdStr = principal.getName();

        // 4) null 검사
        if(userIdStr == null || userIdStr.isBlank()){
            throw new CustomException(ExceptionCode.MISSING_USER_ID);
        }

        // 5) 채팅방 참가 가능여부 확인 메서드를 위한 파라미터 준비
        final Long userId = Long.parseLong(userIdStr);
        final String roomId = destination.substring(StompConstants.DEST_CHAT_ROOM_PREFIX.length());

        // 6) 채팅방 ID가 36자리인지 (UUID인지) 확인
        if(roomId.isBlank() || roomId.length() == StompConstants.ROOM_ID_LEN) {
            throw new CustomException(ExceptionCode.CHAT_ROOM_NOT_EXIST);
        }

        // 7) 채팅방 입장 가능한지 확인
        if(!chatRoomService.canSubscribeChatRoom(roomId, userId)) {
            throw new CustomException(ExceptionCode.CHAT_ROOM_NOT_JOINED);
        }
    }
}
