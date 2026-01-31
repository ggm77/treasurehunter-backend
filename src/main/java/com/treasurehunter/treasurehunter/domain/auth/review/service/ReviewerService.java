package com.treasurehunter.treasurehunter.domain.auth.review.service;

import com.treasurehunter.treasurehunter.domain.auth.review.dto.ReviewerRequestDto;
import com.treasurehunter.treasurehunter.domain.auth.token.dto.TokenResponseDto;
import com.treasurehunter.treasurehunter.domain.user.entity.Role;
import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewerService {

    @Value("${apple.review.id}")
    private String appleId;

    @Value("${apple.review.password}")
    private String applePassword;

    @Value("${apple.review.user-id}")
    private Long appleUserId;

    @Value("${google.review.id}")
    private String googleId;

    @Value("${google.review.password}")
    private String googlePassword;

    @Value("${google.review.user-id}")
    private Long googleUserId;

    private final JwtProvider jwtProvider;

    public TokenResponseDto reviewerLogin(final ReviewerRequestDto reviewerRequestDto) {

        final String id = reviewerRequestDto.getId();
        final String password = reviewerRequestDto.getPassword();

        if(id == null || password == null) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        final String accessToken;
        final String refreshToken;
        if(appleId.equals(id) && applePassword.equals(password)) {
            accessToken = jwtProvider.creatAccessToken(appleUserId, Role.USER);
            refreshToken = jwtProvider.creatRefreshToken(appleUserId);
        }
        else if(googleId.equals(id) && googlePassword.equals(password)) {
            accessToken = jwtProvider.creatAccessToken(googleUserId, Role.USER);
            refreshToken = jwtProvider.creatRefreshToken(googleUserId);
        }
        else {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .tokenType(jwtProvider.getTokenType())
                .exprTime(jwtProvider.getAccessTokenExpirationSeconds())
                .refreshToken(refreshToken)
                .build();
    }
}
