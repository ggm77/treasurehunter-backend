package com.treasurehunter.treasurehunter.domain.chat.controller;

import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.stomp.error.StompErrorSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final StompErrorSender stompErrorSender;

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
