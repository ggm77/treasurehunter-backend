package com.treasurehunter.treasurehunter.domain.auth.token.service;

import com.treasurehunter.treasurehunter.domain.auth.token.dto.RefreshTokenRequestDto;
import com.treasurehunter.treasurehunter.domain.auth.token.dto.TokenResponseDto;
import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import com.treasurehunter.treasurehunter.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    @Value("${jwt.accessToken.exprTime}")
    private Long accessTokenExpireTime;

    @Value("${jwt.refreshToken.exprTime}")
    private Long refreshTokenExpireTime;

    private final CookieUtil cookieUtil;
    private final JwtProvider jwtProvider;

    /**
     * HttpServletRequest에서 쿠키를 가져와 토큰을 찾는 메서드
     * @param httpServletRequest
     * @return TokenResponseDto에 담긴 토큰
     */
    public TokenResponseDto getToken(final HttpServletRequest httpServletRequest) {

        final String accessToken = cookieUtil.getCookieValue(httpServletRequest, "ACCESS_TOKEN");
        final String refreshToken = cookieUtil.getCookieValue(httpServletRequest, "REFRESH_TOKEN");

        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .tokenType(jwtProvider.getTokenType())
                .exprTime(accessTokenExpireTime)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 리프레시 토큰으로 JWT를 재발급하는 메서드
     * @param refreshTokenRequestDto 리프레시 토큰 담겨있는 DTO
     * @return TokenResponseDto에 담긴 토큰
     */
    public TokenResponseDto refreshToken(final RefreshTokenRequestDto refreshTokenRequestDto){

        final String tokenSub = jwtProvider.validateToken(refreshTokenRequestDto.getRefreshToken());
        final Long userId = Long.parseLong(tokenSub);

        final String accessToken = jwtProvider.creatToken(userId,  accessTokenExpireTime);
        final String refreshToken = jwtProvider.creatToken(userId,  refreshTokenExpireTime);

        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .tokenType(jwtProvider.getTokenType())
                .exprTime(accessTokenExpireTime)
                .refreshToken(refreshToken)
                .build();
    }
}
