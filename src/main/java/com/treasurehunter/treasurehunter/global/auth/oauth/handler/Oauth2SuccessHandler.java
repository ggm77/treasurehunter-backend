package com.treasurehunter.treasurehunter.global.auth.oauth.handler;

import com.treasurehunter.treasurehunter.domain.user.domain.Role;
import com.treasurehunter.treasurehunter.domain.user.domain.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class Oauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final String NEW_USER_URI;
    private final String EXISTING_USER_URI;
    private final Long accessTokenExpireTime;
    private final Long refreshTokenExpireTime;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    public Oauth2SuccessHandler(
            @Value("${url.base}") String BASE_URL,
            @Value("${path.new-user}") String NEW_USER_PATH,
            @Value("${path.existing-user}") String EXISTING_USER_PATH,
            @Value("${jwt.accessToken.exprTime}") Long accessTokenExpireTime,
            @Value("${jwt.refreshToken.exprTime}") Long refreshTokenExpireTime,
            final UserRepository userRepository,
            final JwtProvider jwtProvider
    ){
        this.NEW_USER_URI = BASE_URL + NEW_USER_PATH;
        this.EXISTING_USER_URI = BASE_URL + EXISTING_USER_PATH;
        this.accessTokenExpireTime = accessTokenExpireTime;
        this.refreshTokenExpireTime = refreshTokenExpireTime;
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public void onAuthenticationSuccess(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final Authentication authentication
    ) throws IOException, ServletException {
        final OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        //CustomDefaultOauth2User를 통해서 getName()의 결과를 문자열이 된 유저 아이디로 override함
        final Long userId = Long.parseLong(oauth2User.getName());

        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        //엑세스 토큰 발급 (유효 1시간)
        final String accessToken = jwtProvider.creatToken(user.getId(), accessTokenExpireTime);
        //리프레시 토큰 발급 (유효 1일)
        final String refreshToken = jwtProvider.creatToken(user.getId(), refreshTokenExpireTime);

        //엑세스 토큰 담은 쿠키 생성 및 response에 추가
        final Cookie accessTokenCookie = new Cookie("ACCESS_TOKEN", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(accessTokenExpireTime.intValue());
        httpServletResponse.addCookie(accessTokenCookie);

        //리프레시 토큰 담은 쿠키 생성 및 response에 추가
        final Cookie refreshTokenCookie = new Cookie("REFRESH_TOKEN", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(refreshTokenExpireTime.intValue());
        httpServletResponse.addCookie(refreshTokenCookie);

        //리다이렉트할 URI만들고 리다이렉트 시키기
        final String redirectUri = getRedirectUriByRole(user.getRole(), user.getId());
        getRedirectStrategy().sendRedirect(httpServletRequest, httpServletResponse, redirectUri);

    }

    private String getRedirectUriByRole(
            final Role role,
            final Long userId
    ){
        if(role == Role.NOT_REGISTERED){
            return UriComponentsBuilder.fromUriString(NEW_USER_URI)
                    .queryParam("userId", userId)
                    .build()
                    .toUriString();
        }
        return UriComponentsBuilder.fromUriString(EXISTING_USER_URI)
                .queryParam("userId", userId)
                .build()
                .toUriString();
    }
}
