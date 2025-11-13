package com.treasurehunter.treasurehunter.global.stomp.auth;

import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import com.treasurehunter.treasurehunter.global.stomp.dto.StompAuthResultDto;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtStompAuthenticator {

    private final JwtProvider jwtProvider;

    /**
     * 들어온 STOMP 메세지를 바탕으로 JWT 검사를 하는 메서드
     * @param accessor 처리할 STOMP 메세지의 헤더 정보를 포함하는 객체
     * @return AuthResultDto에 담긴 유저 정보
     */
    public StompAuthResultDto authenticateFromAccessor(final StompHeaderAccessor accessor) {
        // 1) 토큰 추출
        final String token = accessor.getFirstNativeHeader("Authorization");

        // 2) 토큰 null 검사
        if (token == null || token.isEmpty()) {
            throw new MessagingException("Missing Authorization header.");
        }

        // 3) Bearer로 시작하는지 검사
        if (!token.startsWith("Bearer ")) {
            throw new MessagingException("Invalid Authorization header.");
        }

        // 4) 토큰 검증 및 유저 ID, 유저 Authorities 추출
        final String jwt = token.substring(7);
        final Claims claims = jwtProvider.getClaims(jwt);
        final String userIdStr = claims.getSubject();
        final List<SimpleGrantedAuthority> authorities = jwtProvider.getAuthorities(claims);
        final long exp = claims.getExpiration().getTime();

        // 5) AuthResultDto로 리턴
        return StompAuthResultDto.builder()
                .userIdStr(userIdStr)
                .authorities(authorities)
                .exp(exp)
                .build();
    }
}
