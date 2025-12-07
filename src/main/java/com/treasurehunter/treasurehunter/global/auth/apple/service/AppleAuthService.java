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
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import com.treasurehunter.treasurehunter.global.util.JsonUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppleAuthService {

    @Value("${server.base-url}")
    private String BASE_URL;

    @Value("${apple.client-id}")
    private String APPLE_CLIENT_ID;

    @Value("${apple.auth.base-url}")
    private String APPLE_AUTH_BASE_URL;

    @Value("${apple.auth.authorize}")
    private String APPLE_AUTHORIZE;

    private final JwtProvider jwtProvider;
    private final AppleKeyGenerator appleKeyGenerator;
    private final AppleAuthClient appleAuthClient;
    private final UserOauth2Service userOauth2Service;
    private final JsonUtil jsonUtil;

    /**
     * 애플의 로그인 창으로 리다이렉트 시키는
     * URL만드는 메서드
     *
     * redirectUrl은 AppleAuthController에 정의된 리다이렉트 API 엔드포인트와
     * 애플 측에 설정된 리다이렉트 URL과 일치해야함.
     * @return 애플 로그인 URL
     */
    public String buildAppleAuthorizeUrl(){
        final String state = jwtProvider.createStateToken();
        final String redirectUrl = BASE_URL + "/login/oauth2/code/apple"; //컨트롤러에 정의 되어있는 값과 똑같이 설정 해야함

        return APPLE_AUTH_BASE_URL+APPLE_AUTHORIZE +
                "?response_type=code%20id_token" +
                "&response_mode=form_post" +
                "&client_id=" + URLEncoder.encode(APPLE_CLIENT_ID, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8) +
                "&scope=name%20email" +
                "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
    }

    /**
     * 애플 로그인 진행하는 메서드
     * 신규 유저라면 자동으로 이름, 이메일, 리프레시 토큰을 DB에 저장한다.
     * @param appleAuthRequestDto 애플 로그인에 필요한 정보 담긴 DTO
     * @return 프론트에 줘야하는 정보가 담긴 DTO
     */
    public AppleAuthResponseDto processAppleAuth(final AppleAuthRequestDto appleAuthRequestDto) {

        // 1) 변수 지정
        final String idToken = appleAuthRequestDto.getId_token();
        final String authorizationCode = appleAuthRequestDto.getCode();
        final String state = appleAuthRequestDto.getState();
        final String rawUser = appleAuthRequestDto.getUser();

        // 2) 필수 요소 검증
        if(idToken == null || idToken.isEmpty() || authorizationCode == null || authorizationCode.isEmpty()){
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 3) state에 든 stateToken 검증
        try {
            jwtProvider.getPayload(state);
        } catch (Exception ex){
            throw new CustomException(ExceptionCode.INVALID_TOKEN);
        }

        // 4) 문자열로 들어온 user가 존재하면 Json으로 파싱
        final Map<String, Object> user;
        if(rawUser != null && !rawUser.isEmpty()){
            try {
                user = jsonUtil.toMap(jsonUtil.removeEscapes(rawUser));
            } catch (IllegalArgumentException ex) {
                throw new CustomException(ExceptionCode.INVALID_REQUEST);
            }
        } else {
            user = Collections.emptyMap();
        }


        // 5) 이름과 이메일이 존재하면 추출
        final Map<String, String> extractedUserInfo;
        try {
            extractedUserInfo = extractUserInfo(user);
        } catch (ClassCastException ex){
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }
        final String name = extractedUserInfo.get("name");
        final String email = extractedUserInfo.get("email");


        // 6) 헤더 추출
        final Map<String, String> headers = jwtProvider.getHeaders(idToken);

        // 7) 애플에 공개키 요청
        final ApplePublicKeyResponseDto applePublicKeyResponseDto = appleAuthClient.requestKeys();

        // 8) 키 조합
        final PublicKey publicKey = appleKeyGenerator.generatePublicKey(
                headers,
                applePublicKeyResponseDto
        );

        // 9) 리프레시 토큰(탈퇴 용) 가져오기
        final AppleTokenResponseDto appleTokenResponseDto = appleAuthClient.requestToken(authorizationCode);
        final String appleRefreshToken = appleTokenResponseDto.getRefresh_token();

        // 10) 애플 아이디 가져오기
        final Claims claims = jwtProvider.getClaimsFromAppleToken(idToken, publicKey);
        final String accountId = claims.getSubject();

        // 11) oauth2 정보 저장용 dto 생성
        final UserOauth2AccountsRequestDto userOauth2AccountsRequestDto = UserOauth2AccountsRequestDto.builder()
                .provider("apple")
                .providerUserId(accountId)
                .email(email)
                .name(name)
                .profileImage(null)
                .refreshToken(appleRefreshToken)
                .build();

        // 12) 신규 유저면 DB에 정보 저장하고 정보 조회, 기존 유저면 그냥 정보만 조회
        final UserOauth2AccountsResponseDto userOauth2AccountsResponseDto = userOauth2Service.upsertOAuthUser(userOauth2AccountsRequestDto);

        // 13) 가져온 정보에서 정보 추출
        final Long userId = userOauth2AccountsResponseDto.getUserId();
        final Role role = userOauth2AccountsResponseDto.getUserRole();

        // 14) JWT발급
        final String accessToken = jwtProvider.creatAccessToken(userId, role);
        final String refreshToken = jwtProvider.creatRefreshToken(userId);

        return AppleAuthResponseDto.builder()
                .userId(userId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(role)
                .build();
    }

    //애플에게 받은 user에서 정보가 존재할 때 추출하기 위한 메서드
    private Map<String, String> extractUserInfo(final Map<String, Object> user) {

        final String email;
        final String name;

        //정보가 완전하지 않으면 무조건 null, 정보가 완전하면 문자열로 된 정보
        if(!user.isEmpty()){
            if(user.get("email") != null){
                email = user.get("email").toString();
            } else {
                email = null;
            }

            if(user.get("name") != null){
                final Map<String, Object> nameMap = (Map<String, Object>) user.get("name");
                final String firstName = (String) nameMap.get("firstName");
                final String lastName = (String) nameMap.get("lastName");
                if(firstName != null && lastName != null){
                    name = firstName + " " + lastName;
                } else {
                    name = null;
                }
            } else {
                name = null;
            }
        } else {
            name = null;
            email = null;
        }

        final Map<String, String> result = new HashMap<>();
        result.put("email", email);
        result.put("name", name);

        return result;
    }
}
