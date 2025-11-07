package com.treasurehunter.treasurehunter.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    // 엔트포인트 등록
    @Override
    public void registerStompEndpoints(final StompEndpointRegistry stompEndpointRegistry){
        stompEndpointRegistry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins) //cors 설정
                .withSockJS();
    }

    // prefix로 sub이 붙으면 구독, pub이 붙으면 메세지 송신
    @Override
    public void configureMessageBroker(final MessageBrokerRegistry messageBrokerRegistry){
        messageBrokerRegistry.enableSimpleBroker("/sub");
        messageBrokerRegistry.setApplicationDestinationPrefixes("/pub");
    }
}
