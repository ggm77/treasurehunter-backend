package com.treasurehunter.treasurehunter.global.auth.oauth.handler;

import com.treasurehunter.treasurehunter.domain.user.entity.Role;
import com.treasurehunter.treasurehunter.domain.user.service.oauth.UserOauth2Service;
import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import com.treasurehunter.treasurehunter.global.auth.oauth.CustomDefaultOauth2User;
import com.treasurehunter.treasurehunter.global.auth.oauth.dto.UserOauth2AccountsRequestDto;
import com.treasurehunter.treasurehunter.global.auth.oauth.dto.UserOauth2AccountsResponseDto;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import com.treasurehunter.treasurehunter.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * OAuth 인증에 성공한 경우를 처리하는 핸들러
 * 이미 등록된 유저면 JWT를 쿠키에 담아준 후 특정 페이지로 리다이렉트
 * 신규 유저면 JWT를 쿠키에 담아주고 특정 페이지로 리다이렉트
 */
@Component
public class Oauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final CookieUtil cookieUtil;
    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private final UserOauth2Service userOauth2Service;

    public Oauth2SuccessHandler(
            final JwtProvider jwtProvider,
            final CookieUtil cookieUtil,
            final OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
            final UserOauth2Service userOauth2Service
    ){
        this.jwtProvider = jwtProvider;
        this.cookieUtil = cookieUtil;
        this.oAuth2AuthorizedClientService = oAuth2AuthorizedClientService;
        this.userOauth2Service = userOauth2Service;
    }

    @Override
    public void onAuthenticationSuccess(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final Authentication authentication
    ) throws IOException {

        //Authentication 객체를 OAuth2 작업을 할 수 있도록 OAuth2User로 캐스팅
        final OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        //어트리뷰트 변수에 저장
        final Map<String, Object> attributes = oauth2User.getAttributes();

        //provider를 얻기 위해서 잠시 캐스팅
        final String provider = ((CustomDefaultOauth2User) oauth2User).getProvider();

        //리프레시 토큰 추출을 위해 캐스팅
        final OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        final OAuth2AuthorizedClient oAuth2AuthorizedClient = oAuth2AuthorizedClientService.loadAuthorizedClient(oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());

        //리프레시 토큰 추출, 없으면 null
        final String oauth2RefreshToken;
        if(oAuth2AuthorizedClient != null && oAuth2AuthorizedClient.getRefreshToken() != null) {
            oauth2RefreshToken = oAuth2AuthorizedClient.getRefreshToken().getTokenValue();
        } else {
            oauth2RefreshToken = null;
        }

        //정보 담을 DTO 생성
        final UserOauth2AccountsRequestDto userOauth2AccountsRequestDto;

        //provider에 따라서 적절히 처리
        switch (provider) {
            case "google" -> {

                userOauth2AccountsRequestDto = UserOauth2AccountsRequestDto.builder()
                        .provider(provider)
                        .providerUserId( (String) attributes.get("sub") )
                        .email( (String) attributes.get("email") )
                        .name( (String) attributes.getOrDefault("name", null) )
                        .profileImage((String) attributes.get("picture") )
                        .refreshToken(oauth2RefreshToken)
                        .build();
            }
//            case "naver" -> {}
            default -> throw new CustomException(ExceptionCode.UNSUPPORTED_PROVIDER);
        }

        // 정보가 존재하면 로그인, 없으면 유저 등록만 하는 메서드
        final UserOauth2AccountsResponseDto userOauth2AccountsResponseDto = userOauth2Service.upsertOAuthUser(userOauth2AccountsRequestDto);

        //등록하거나 조회한 유저 ID와 Role 가져오기
        final Long userId = userOauth2AccountsResponseDto.getUserId();
        final Role userRole = userOauth2AccountsResponseDto.getUserRole();

        //엑세스 토큰 발급 (유효 1시간)
        final String accessToken = jwtProvider.creatAccessToken(userId, userRole);
        //리프레시 토큰 발급 (유효 1일)
        final String refreshToken = jwtProvider.creatRefreshToken(userId);

        final Map<String, ResponseCookie> jwtCookie = cookieUtil.createJwtCookie(accessToken, refreshToken);

        final ResponseCookie accessTokenCookie = jwtCookie.get("accessToken");
        final ResponseCookie refreshTokenCookie = jwtCookie.get("refreshToken");

        //쿠키 response에 추가
        httpServletResponse.addHeader("Set-Cookie", accessTokenCookie.toString());
        httpServletResponse.addHeader("Set-Cookie", refreshTokenCookie.toString());

        //리다이렉트할 URI만들고 리다이렉트 시키기
        final String redirectUri = cookieUtil.getRedirectUriByRole(userRole, userId);
        getRedirectStrategy().sendRedirect(httpServletRequest, httpServletResponse, redirectUri);
    }


}
