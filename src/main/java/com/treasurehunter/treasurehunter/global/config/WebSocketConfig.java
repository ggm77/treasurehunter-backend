package com.treasurehunter.treasurehunter.global.config;

import com.treasurehunter.treasurehunter.global.auth.interceptor.StompAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${stomp.relay.host}")
    private String RELAY_HOST;

    @Value("${stomp.relay.port}")
    private Integer RELAY_PORT;

    @Value("${stomp.relay.username}")
    private String RELAY_USERNAME;

    @Value("${stomp.relay.password}")
    private String RELAY_PASSWORD;

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    // 엔트포인트 등록
    @Override
    public void registerStompEndpoints(final StompEndpointRegistry stompEndpointRegistry){
        stompEndpointRegistry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins) //cors 설정
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(final MessageBrokerRegistry messageBrokerRegistry){

        // 클라이언트 -> 서버
        messageBrokerRegistry.setApplicationDestinationPrefixes("/app");

        // 서버 -> 클라이언트
        messageBrokerRegistry.setUserDestinationPrefix("/user"); //개인 에러 전송용
        messageBrokerRegistry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(RELAY_HOST)
                .setRelayPort(RELAY_PORT)
                .setSystemLogin(RELAY_USERNAME)
                .setSystemPasscode(RELAY_PASSWORD)
                .setClientLogin(RELAY_USERNAME)
                .setClientPasscode(RELAY_PASSWORD);
    }

    // 웹소켓에서 인증 인가를 처리하기 위함
    // 필터와 비슷한 역할
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }
}
