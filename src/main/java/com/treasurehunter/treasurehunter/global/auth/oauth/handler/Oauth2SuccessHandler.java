package com.treasurehunter.treasurehunter.global.auth.oauth.handler;

import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import com.treasurehunter.treasurehunter.global.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;

/**
 * OAuth 인증에 성공한 경우를 처리하는 핸들러
 * 이미 등록된 유저면 JWT를 쿠키에 담아준 후 특정 페이지로 리다이렉트
 * 신규 유저면 JWT를 쿠키에 담아주고 특정 페이지로 리다이렉트
 */
@Component
public class Oauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final CookieUtil cookieUtil;

    public Oauth2SuccessHandler(
            final UserRepository userRepository,
            final JwtProvider jwtProvider,
            final CookieUtil cookieUtil
    ){
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.cookieUtil = cookieUtil;
    }

    @Override
    @Transactional
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
        final String accessToken = jwtProvider.creatAccessToken(user.getId(), user.getRole());
        //리프레시 토큰 발급 (유효 1일)
        final String refreshToken = jwtProvider.creatRefreshToken(user.getId());

        final Map<String, ResponseCookie> jwtCookie = cookieUtil.createJwtCookie(accessToken, refreshToken);

        final ResponseCookie accessTokenCookie = jwtCookie.get("accessToken");
        final ResponseCookie refreshTokenCookie = jwtCookie.get("refreshToken");

        //쿠키 response에 추가
        httpServletResponse.addHeader("Set-Cookie", accessTokenCookie.toString());
        httpServletResponse.addHeader("Set-Cookie", refreshTokenCookie.toString());

        //리다이렉트할 URI만들고 리다이렉트 시키기
        final String redirectUri = cookieUtil.getRedirectUriByRole(user.getRole(), user.getId());
        getRedirectStrategy().sendRedirect(httpServletRequest, httpServletResponse, redirectUri);
    }


}
