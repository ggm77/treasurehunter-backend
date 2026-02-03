package com.treasurehunter.treasurehunter.domain.auth.oauth2.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.treasurehunter.treasurehunter.domain.auth.oauth2.dto.Oauth2RequestDto;
import com.treasurehunter.treasurehunter.domain.auth.oauth2.dto.Oauth2ResponseDto;
import com.treasurehunter.treasurehunter.domain.user.dto.oauth.UserOauth2AccountsRequestDto;
import com.treasurehunter.treasurehunter.domain.user.dto.oauth.UserOauth2AccountsResponseDto;
import com.treasurehunter.treasurehunter.domain.user.entity.Role;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.domain.user.repository.oauth.UserOauth2AccountsRepository;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class Oauth2Service {

    @Value("${google.web.client-id}")
    private String WEB_CLIENT_ID;

    @Value("${google.web.client-secret}")
    private String WEB_CLIENT_SECRET;

    private final UserOauth2Service userOauth2Service;
    private final JwtProvider jwtProvider;
    private final AppleAuthClient appleAuthClient;
    private final AppleKeyGenerator appleKeyGenerator;
    private final UserOauth2AccountsRepository userOauth2AccountsRepository;
    private final UserRepository userRepository;

    /**
     * 구글 OAuth 관련 이벤트 발생하면 처리하는 메서드
     * @param token 구글에서 보내는 정보가 담긴 토큰
     */
    @Transactional
    public void handleGoogleRiscEvent(final String token) {
        try {
            // 1. 구글 공개키를 사용하여 토큰 검증
            final GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(WEB_CLIENT_ID))
                    .build();

            final GoogleIdToken idToken = verifier.verify(token);
            if (idToken == null) {
                log.error("Invalid Google RISC token");
                return;
            }

            // 2. 이벤트 타입 확인 (계정 삭제 또는 토큰 취소)
            final String payload = idToken.getPayload().toString();
            if (payload.contains("account-purged") || payload.contains("tokens-revoked")) {
                final String providerUserId = idToken.getPayload().getSubject(); // 구글의 sub(식별자)

                // 3. DB에서 해당 구글 계정 정보를 조회
                userOauth2AccountsRepository.findByProviderAndProviderUserId("google", providerUserId)
                        .ifPresent(account -> {
                            final User user = account.getUser();
                            final Long userId = user.getId();
                            // 4. 유저 삭제 (CascadeType.ALL에 의해 연관된 OAuth 계정도 삭제됨)
                            userRepository.delete(user);
                            log.info("User deleted by Google RISC request: {}", userId);
                        });
            }
        } catch (Exception e) {
            log.error("Error processing Google RISC event", e);
        }
    }

    /**
     * 애플 OAuth 관련 이벤트 발생하면 처리하는 메서드
     * @param payload 애플 측에서 보내는 정보
     */
    @Transactional
    public void handleAppleSpsEvent(final String payload) {
        try {
            // 1. 애플 공개키 가져오기 및 검증
            final Map<String, String> headers = jwtProvider.getHeaders(payload);
            final ApplePublicKeyResponseDto applePublicKeyResponseDto = appleAuthClient.requestKeys();
            final PublicKey publicKey = appleKeyGenerator.generatePublicKey(headers, applePublicKeyResponseDto);

            // 2. JWT 파싱 및 클레임 추출
            final Claims claims = jwtProvider.getClaimsFromAppleToken(payload, publicKey);

            // 3. 이벤트 타입 확인 (consent-revoked: 앱 연결 해제)
            final String eventType = claims.get("type", String.class);
            if ("consent-revoked".equals(eventType)) {
                final String providerUserId = claims.getSubject(); // 애플의 sub

                // 4. DB 조회 및 유저 삭제
                userOauth2AccountsRepository.findByProviderAndProviderUserId("apple", providerUserId)
                        .ifPresent(account -> {
                            final User user = account.getUser();
                            final Long userId = user.getId();
                            userRepository.delete(user);
                            log.info("User deleted by Apple SPS request: {}", userId);
                        });
            }
        } catch (Exception e) {
            log.error("Error processing Apple SPS event", e);
        }
    }

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
