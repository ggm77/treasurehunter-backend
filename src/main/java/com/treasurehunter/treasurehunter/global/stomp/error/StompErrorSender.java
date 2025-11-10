package com.treasurehunter.treasurehunter.global.stomp.error;

import com.treasurehunter.treasurehunter.domain.chat.dto.error.ChatErrorDto;
import com.treasurehunter.treasurehunter.global.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Principal;

@Component
@RequiredArgsConstructor
public class StompErrorSender {

    //순환 참조 방지 지연 주입
    @Qualifier("clientOutboundChannel")
    private final ObjectProvider<MessageChannel> clientOutboundChannel;
    //순환 참조 방지 지연 주입
    private final ObjectProvider<SimpMessagingTemplate> simpMessagingTemplate;

    private final JsonUtil jsonUtil;


    /**
     * 세션에 에러 메세지를 보내는 메서드
     * @param sessionId 오류 메세지 보낼 세션 ID
     * @param code 오류 코드
     * @param message 오류에 대한 설명
     * @param destination 오류가 난 요청의 주소
     */
    public void toSession(
            final String sessionId,
            final String code,
            final String message,
            final String destination
    ){
        // 1) 오류 메세지 DTO 빌드
        final ChatErrorDto chatErrorDto = ChatErrorDto.builder()
                .code(code)
                .message(message)
                .destination(destination)
                .build();

        // 2) 메세지 헤더 설정
        final StompHeaderAccessor error = StompHeaderAccessor.create(StompCommand.ERROR);
        error.setSessionId(sessionId);
        error.setMessage(code);
        error.setLeaveMutable(true);

        // 3) 메세지 본문 설정
        final Message<byte[]> payload = MessageBuilder
                .withPayload(jsonUtil.toJson(chatErrorDto).getBytes(StandardCharsets.UTF_8))
                .copyHeaders(error.getMessageHeaders())
                .build();

        // 4) 메세지 전송
        clientOutboundChannel.getObject()
                .send(payload);
    }

    /**
     * 유저에게 에러 발송하는 메서드
     * @param principal 에러 낸 유저 정보
     * @param code 에러 유형
     * @param message 에러 메세지
     */
    public void toUser(
            final Principal principal,
            final String code,
            final String message,
            final String destination
    ) {
        // 1) 유저 정보 없는 경우 에러 보내지 않음
        if (principal == null){
            return;
        }

        // 2) 오류 메세지 DTO 빌드
        final ChatErrorDto chatErrorDto = ChatErrorDto.builder()
                .code(code)
                .message(message)
                .destination(destination)
                .build();

        // 3) 오류 메세지 전송
        simpMessagingTemplate.getObject()
                .convertAndSendToUser(principal.getName(), "/queue/error", chatErrorDto);
    }
}
