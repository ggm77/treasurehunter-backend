package com.treasurehunter.treasurehunter.global.stomp.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

@Getter
public class StompSessionDto {
    private final String sessionId;
    private final String userIdStr;
    private final String destination;

    @Builder
    public StompSessionDto(
            final String sessionId,
            final String userIdStr,
            final String destination
    ){
        this.sessionId = sessionId;
        this.userIdStr = userIdStr;
        this.destination = destination;
    }

    public StompSessionDto(final StompHeaderAccessor accessor) {
        this.sessionId = accessor.getSessionId();
        this.userIdStr = accessor.getUser().getName();
        this.destination = accessor.getDestination();
    }
}
