package com.treasurehunter.treasurehunter.domain.chat.controller;

import com.treasurehunter.treasurehunter.domain.chat.dto.ChatRequestDto;
import com.treasurehunter.treasurehunter.domain.chat.dto.ChatResponseDto;
import com.treasurehunter.treasurehunter.domain.chat.service.ChatService;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.stomp.error.StompErrorSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final StompErrorSender stompErrorSender;

    //클라이언트가 메세지를 전송하는 경우
    @MessageMapping("/chat.room.{id}")
    public void send(
            @DestinationVariable("id") final String roomId,
            @Validated @Payload final ChatRequestDto chatRequestDto,
            final Principal principal,
            @Header("simpSessionId") final String sessionId
    ){
        final ChatResponseDto sentChat = chatService.saveMessage(roomId, chatRequestDto, principal, sessionId);

        simpMessagingTemplate.convertAndSend("/topic/chat.room."+sentChat.getRoomId(), sentChat);
    }

    //처리중에 낸 예외 처리용
    @MessageExceptionHandler(CustomException.class)
    public void handleIllegalArg(
            final CustomException ex,
            final Message<?> message,
            final Principal principal
    ) {
        //요청한 주소 추출
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        final String destination = accessor.getDestination();

        //CustomException은 ResponseEntity를 응답하기 때문에 정보만 가져오기
        stompErrorSender.toUser(
                principal,
                ex.getExceptionCode().name(),
                ex.getExceptionCode().getMessage(),
                destination
        );
    }

    //처리중에 일어난 예외 처리용
    @MessageExceptionHandler(Exception.class)
    public void handleGeneric(
            final Exception ex,
            final Message<?> message,
            final Principal principal
    ) {
        //요청한 주소 추출
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        final String destination = accessor.getDestination();

        log.warn("STOMP ERROR:", ex);

        stompErrorSender.toUser(
                principal,
                "INTERNAL_ERROR",
                "처리 중 오류가 발생했습니다",
                destination
        );
    }
}
