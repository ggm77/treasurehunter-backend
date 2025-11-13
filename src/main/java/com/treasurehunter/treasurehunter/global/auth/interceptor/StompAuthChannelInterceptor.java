package com.treasurehunter.treasurehunter.global.auth.interceptor;

import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import com.treasurehunter.treasurehunter.global.stomp.auth.JwtStompAuthenticator;
import com.treasurehunter.treasurehunter.global.stomp.auth.StompSessionContext;
import com.treasurehunter.treasurehunter.global.stomp.constants.StompConstants;
import com.treasurehunter.treasurehunter.global.stomp.dto.StompAuthResultDto;
import com.treasurehunter.treasurehunter.global.stomp.error.StompErrorSender;
import com.treasurehunter.treasurehunter.global.stomp.subscribe.StompSubscribeGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * Stomp 통신 과정에서 인증 인가를 처리하는 메서드
 * CONNECT에서만 JWT를 통한 인증 후 세션 인증한다.
 * 이후 과정에서는 JWT를 사용하지 않음.
 * 세션 인증시 JWT와 같은 만료 시간을 설정해서 JWT가 만료 되면 세션도 함꼐 만료 되게 함
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtStompAuthenticator jwtStompAuthenticator;
    private final StompSessionContext stompSessionContext;
    private final StompSubscribeGuard stompSubscribeGuard;
    private final StompErrorSender stompErrorSender;

    @Override
    public Message<?> preSend(
            final Message<?> message,
            final MessageChannel channel
    ){
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        try {
            // 0) HEARTBEAT 예외처리
            if(accessor.getCommand() == null) {
                return message;
            }

            //STOMP가 Connect 단계일 경우
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {

                // 1) jwt인증
                final StompAuthResultDto stompAuthResultDto = jwtStompAuthenticator.authenticateFromAccessor(accessor);

                // 2) 세션에 정보 저장
                stompSessionContext.store(accessor, stompAuthResultDto);

                // 3) 접속 로깅
                log.info("STOMP CONNECT by userId: {}, sessionId: {}", stompAuthResultDto.getUserIdStr(), accessor.getSessionId());

                // 4) 메세지에 내용 저장을 확실히 하기 위해서 새로운 메세지로 리턴
                return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
            }
            //STOMP가 Connect 단계가 아닌 다른 모든 경우에는 JWT검사 X
            else {
                // 1) 세션이 인증 되어있는지 검사
                //Principal이 없다면 attributes에서 복원
                if (accessor.getUser() == null) {
                    // 인증 안되어있으면 예외 터짐
                    stompSessionContext.restorePrincipalFromAttributes(accessor);
                }

                // 2) 세션 만료 시간 검사 (만료시 CustomException 터짐)
                stompSessionContext.assertNotExpired(accessor);
            }

            //STOMP가 SUBSCRIBE일 경우 채팅방에 입장 가능한지 검사
            if(StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                // 1) 요청 주소 가져오기
                final String destination = accessor.getDestination();

                // 2) null 검사
                if (destination == null || destination.isEmpty()) {
                    throw new CustomException(ExceptionCode.INVALID_REQUEST);
                }

                //채팅방 구독일 경우 채팅방에 입장 가능한지 확인
                if(destination.startsWith(StompConstants.DEST_CHAT_ROOM_PREFIX)) {
                    stompSubscribeGuard.assertCanSubscribeChatRoom(accessor, destination);
                }
            }

            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
        }
        //에러 메세지를 따로 보내기 위함
        catch (CustomException ex){
            // 1) 세션 아이디 지정
            final String sessionId = accessor.getSessionId();
            final String destination = accessor.getDestination();

            // 2) 세션 아이디 없으면 종료
            if(sessionId == null){
                return null;
            }

            // 3) 로깅
            log.warn("STOMP client error: {}", ex.getExceptionCode().name());

            // 4) 에러 전송
            stompErrorSender.toSession(
                    sessionId,
                    ex.getExceptionCode().name(),
                    ex.getExceptionCode().getMessage(),
                    destination
            );

            return null;
        }
        catch (Exception ex) {

            // 1) 세션 아이디 지정
            final String sessionId = accessor.getSessionId();
            final String destination = accessor.getDestination();

            // 2) 세션 아이디 없으면 종료
            if(sessionId == null){
                return null;
            }

            // 3) 로깅
            log.error("STOMP client error: {}", ex.getMessage(), ex);

            // 4) 에러 전송
            stompErrorSender.toSession(
                    sessionId,
                    "INTERNAL_SERVER_ERROR",
                    "INTERNAL_SERVER_ERROR",
                    destination);

            return null;
        }
    }
}
