package com.treasurehunter.treasurehunter.domain.auth.service;

import com.treasurehunter.treasurehunter.domain.auth.dto.DevJwtRequestDto;
import com.treasurehunter.treasurehunter.domain.auth.dto.DevJwtResponseDto;
import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DevJwtService {

    @Value("${jwt.accessToken.exprTime}")
    private Long accessTokenExpireTime;

    @Value("${jwt.refreshToken.exprTime}")
    private Long refreshTokenExpireTime;

    private final JwtProvider jwtProvider;

    public DevJwtResponseDto createJwt(final DevJwtRequestDto devJwtRequestDto){

        final Long userId = devJwtRequestDto.getUserId();

        //엑세스 토큰 발급 (유효 1시간)
        final String accessToken = jwtProvider.creatToken(userId, accessTokenExpireTime);
        //리프레시 토큰 발급 (유효 1일)
        final String refreshToken = jwtProvider.creatToken(userId, refreshTokenExpireTime);

        return DevJwtResponseDto.builder()
                .accessToken(accessToken)
                .tokenType(jwtProvider.getTokenType())
                .exprTime(accessTokenExpireTime)
                .refreshToken(refreshToken)
                .build();
    }
}
