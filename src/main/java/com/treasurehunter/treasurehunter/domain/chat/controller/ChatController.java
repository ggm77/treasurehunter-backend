package com.treasurehunter.treasurehunter.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    @MessageMapping("/send")
    @SendTo("/sub/messages")
    public String sendMessage(final String inputMessage){
        log.info("메세지 들어옴: {}", inputMessage);

        return inputMessage;
    }
}
