package com.treasurehunter.treasurehunter.global.auth.apple.service;

import com.treasurehunter.treasurehunter.domain.user.entity.Role;
import com.treasurehunter.treasurehunter.domain.user.service.oauth.UserOauth2Service;
import com.treasurehunter.treasurehunter.global.auth.apple.dto.auth.AppleAuthRequestDto;
import com.treasurehunter.treasurehunter.global.auth.apple.dto.auth.AppleAuthResponseDto;
import com.treasurehunter.treasurehunter.global.auth.apple.dto.key.ApplePublicKeyResponseDto;
import com.treasurehunter.treasurehunter.global.auth.apple.dto.token.AppleTokenResponseDto;
import com.treasurehunter.treasurehunter.global.auth.apple.feign.AppleAuthClient;
import com.treasurehunter.treasurehunter.global.auth.apple.util.AppleKeyGenerator;
import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import com.treasurehunter.treasurehunter.global.auth.oauth.dto.UserOauth2AccountsRequestDto;
import com.treasurehunter.treasurehunter.global.auth.oauth.dto.UserOauth2AccountsResponseDto;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppleAuthService {

    private final JwtProvider jwtProvider;
    private final AppleKeyGenerator appleKeyGenerator;
    private final AppleAuthClient appleAuthClient;
    private final UserOauth2Service userOauth2Service;

    /**
     * 애플 로그인 진행하는 메서드
     * 신규 유저라면 자동으로 이름, 이메일, 리프레시 토큰을 DB에 저장한다.
     * @param appleAuthRequestDto 애플 로그인에 필요한 정보 담긴 DTO
     * @return 프론트에 줘야하는 정보가 담긴 DTO
     */
    public AppleAuthResponseDto processAppleAuth(final AppleAuthRequestDto appleAuthRequestDto) {

        // 1) 변수 지정
        final String idToken = appleAuthRequestDto.getIdToken();
        final String authorizationCode = appleAuthRequestDto.getAuthorizationCode();
        final String name = appleAuthRequestDto.getFirstName() + " " + appleAuthRequestDto.getLastName();
        final String email = appleAuthRequestDto.getEmail();

        // 2) 헤더 추출
        final Map<String, String> headers = jwtProvider.getHeaders(idToken);

        // 3) 애플에 공개키 요청
        final ApplePublicKeyResponseDto applePublicKeyResponseDto = appleAuthClient.requestKeys();

        // 4) 키 조합
        final PublicKey publicKey = appleKeyGenerator.generatePublicKey(
                headers,
                applePublicKeyResponseDto
        );

        // 5) 리프레시 토큰(탈퇴 용) 가져오기
        final AppleTokenResponseDto appleTokenResponseDto = appleAuthClient.requestToken(authorizationCode);
        final String appleRefreshToken = appleTokenResponseDto.getRefresh_token();

        // 6) 애플 아이디 가져오기
        final Claims claims = jwtProvider.getClaimsFromAppleToken(idToken, publicKey);
        final String accountId = claims.getSubject();

        // 7) oauth2 정보 저장용 dto 생성
        final UserOauth2AccountsRequestDto userOauth2AccountsRequestDto = UserOauth2AccountsRequestDto.builder()
                .provider("apple")
                .providerUserId(accountId)
                .email(email)
                .name(name)
                .profileImage(null)
                .refreshToken(appleRefreshToken)
                .build();

        // 8) 신규 유저면 DB에 정보 저장하고 정보 조회, 기존 유저면 그냥 정보만 조회
        final UserOauth2AccountsResponseDto userOauth2AccountsResponseDto = userOauth2Service.upsertOAuthUser(userOauth2AccountsRequestDto);

        // 9) 가져온 정보에서 정보 추출
        final Long userId = userOauth2AccountsResponseDto.getUserId();
        final Role role = userOauth2AccountsResponseDto.getUserRole();

        // 10) JWT발급
        final String accessToken = jwtProvider.creatAccessToken(userId, role);
        final String refreshToken = jwtProvider.creatRefreshToken(userId);

        return AppleAuthResponseDto.builder()
                .userId(userId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(role)
                .build();
    }
}
