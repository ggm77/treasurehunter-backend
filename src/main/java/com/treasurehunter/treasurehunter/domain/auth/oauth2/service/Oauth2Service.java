package com.treasurehunter.treasurehunter.domain.auth.oauth2.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.treasurehunter.treasurehunter.domain.auth.oauth2.dto.Oauth2RequestDto;
import com.treasurehunter.treasurehunter.domain.auth.oauth2.dto.Oauth2ResponseDto;
import com.treasurehunter.treasurehunter.domain.user.dto.oauth.UserOauth2AccountsRequestDto;
import com.treasurehunter.treasurehunter.domain.user.dto.oauth.UserOauth2AccountsResponseDto;
import com.treasurehunter.treasurehunter.domain.user.entity.Role;
import com.treasurehunter.treasurehunter.domain.user.service.oauth.UserOauth2Service;
import com.treasurehunter.treasurehunter.global.auth.apple.dto.key.ApplePublicKeyResponseDto;
import com.treasurehunter.treasurehunter.global.auth.apple.dto.token.AppleTokenResponseDto;
import com.treasurehunter.treasurehunter.global.auth.apple.feign.AppleAuthClient;
import com.treasurehunter.treasurehunter.global.auth.apple.util.AppleKeyGenerator;
import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class Oauth2Service {

    @Value("${google.web.client-id}")
    private String WEB_CLIENT_ID;

    @Value("${google.web.client-secret}")
    private String WEB_CLIENT_SECRET;

    private final UserOauth2Service userOauth2Service;
    private final JwtProvider jwtProvider;
    private final AppleAuthClient appleAuthClient;
    private final AppleKeyGenerator appleKeyGenerator;

    /**
     * OAuth2를 진행하는 메서드
     * 프론트에서 code를 받아와서 OAuth2를 진행한다.
     * 인증이 완료되면 JWT를 발급한다.
     * @param oauth2RequestDto code와 provider가 담긴 DTO
     * @return JWT
     */
    public Oauth2ResponseDto processOauth2(final Oauth2RequestDto oauth2RequestDto) {

        // 1) code와 provider 변수에 저장
        final String code = oauth2RequestDto.getCode();
        final String provider = oauth2RequestDto.getProvider();

        // 2) code 검사
        if(code == null || code.isEmpty()) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 3) provider 검사
        if(provider == null || provider.isEmpty()) {
            throw new CustomException(ExceptionCode.INVALID_PROVIDER);
        }

        // 4) provider에 따라서 OAuth2 처리
        final UserOauth2AccountsResponseDto userOauth2AccountsResponseDto;
        if("google".equalsIgnoreCase(provider)) {
            userOauth2AccountsResponseDto = googleOauth2(code);
        }
        else if ("apple".equalsIgnoreCase(provider)) {
            userOauth2AccountsResponseDto = appleOauth2(code, oauth2RequestDto.getName());
        }
        else {
            throw new CustomException(ExceptionCode.INVALID_PROVIDER);
        }

        // 5) 유저 아이디와 role 변수에 저장
        final Long userId = userOauth2AccountsResponseDto.getUserId();
        final Role userRole = userOauth2AccountsResponseDto.getUserRole();

        // 6) 유저 아이디와 role로 JWT만들기
        final String accessToken = jwtProvider.creatAccessToken(userId, userRole);
        final String refreshToken = jwtProvider.creatRefreshToken(userId);

        return Oauth2ResponseDto.builder()
                .role(userRole)
                .accessToken(accessToken)
                .tokenType(jwtProvider.getTokenType())
                .exprTime(jwtProvider.getAccessTokenExpirationSeconds())
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 프론트에서 받은 code를 통해 유저 정보를 조회하고 등록하는 메서드
     * UserOauth2Service.upsertOAuthUser 메서드를 통해서 유저 upsert를 진행함
     * @param code 프론트에서 받은 code
     * @return upsert된 유저의 정보가 담긴 DTO
     */
    private UserOauth2AccountsResponseDto googleOauth2(final String code){

        // 1) code를 통해 구글에서 리프레시 토큰과 유저 정보 조회
        final GoogleTokenResponse response;
        try {
            response = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    "https://oauth2.googleapis.com/token",
                    WEB_CLIENT_ID,
                    WEB_CLIENT_SECRET,
                    code,
                    ""
            ).execute();
        } catch (IOException ex) {
            throw new CustomException(ExceptionCode.GOOGLE_REQUEST_ERROR);
        }

        // 2) 리프레시 토큰 변수에 저장
        final String googleRefreshToken = response.getRefreshToken();

        // 2) 받아온 idToken에서 payload 추출
        final GoogleIdToken.Payload payload;
        try {
            final GoogleIdToken idToken = response.parseIdToken();

            //검증 성공/실패 확인
            if(idToken == null) {
                throw new CustomException(ExceptionCode.INVALID_TOKEN);
            }

            payload = idToken.getPayload();

        } catch (IOException ex){
            throw new CustomException(ExceptionCode.GOOGLE_REQUEST_ERROR);
        }

        // 3) payload에서 정보 추출해서 DTO에 정보 주입
        final UserOauth2AccountsRequestDto userOauth2AccountsRequestDto = UserOauth2AccountsRequestDto.builder()
                .provider("google")
                .providerUserId(payload.getSubject())
                .email(payload.getEmail())
                .name((String) payload.get("name"))
                .profileImage((String) payload.get("picture"))
                .refreshToken(googleRefreshToken)
                .build();

        // 4) 유저 정보에 upsert
        return userOauth2Service.upsertOAuthUser(userOauth2AccountsRequestDto);
    }

    /**
     * 프론트에서 받은 code를 통해서 애플 OAuth2를 진행하는 메서드
     * UserOauth2Service.upsertOAuthUser 메서드를 통해서 유저 upsert를 진행함
     * @param code 프론트에서 받은 code
     * @param name 프론트에서 애플 인증하고 받은 이름
     * @return upsert된 유저의 정보가 담긴 DTO
     */
    private UserOauth2AccountsResponseDto appleOauth2(
            final String code,
            final String name
    ){
        // 1) idToken과 리프레시 토큰(탈퇴 용) 가져오기
        final AppleTokenResponseDto appleTokenResponseDto = appleAuthClient.requestToken(code);
        final String idToken = appleTokenResponseDto.getId_token();
        final String appleRefreshToken = appleTokenResponseDto.getRefresh_token();

        // 2) 헤더 추출
        final Map<String, String> headers = jwtProvider.getHeaders(idToken);

        // 3) 애플에 공개키 요청
        final ApplePublicKeyResponseDto applePublicKeyResponseDto = appleAuthClient.requestKeys();

        // 4) 키 조합
        final PublicKey publicKey = appleKeyGenerator.generatePublicKey(
                headers,
                applePublicKeyResponseDto
        );

        // 5) 애플 아이디와 이메일 가져오기
        final Claims claims = jwtProvider.getClaimsFromAppleToken(idToken, publicKey);
        final String accountId = claims.getSubject();
        final String email = claims.get("email", String.class);

        // 6) oauth2 정보 저장용 dto 생성
        final UserOauth2AccountsRequestDto userOauth2AccountsRequestDto = UserOauth2AccountsRequestDto.builder()
                .provider("apple")
                .providerUserId(accountId)
                .email(email)
                .name(name)
                .profileImage(null)
                .refreshToken(appleRefreshToken)
                .build();

        // 7) 신규 유저면 DB에 정보 저장하고 정보 조회, 기존 유저면 그냥 정보만 조회
        return userOauth2Service.upsertOAuthUser(userOauth2AccountsRequestDto);

    }
}
