package com.treasurehunter.treasurehunter.global.auth.oauth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth가 실패한 경우를 처리하는 핸들러
 * 실패시 특정 페이지로 리다이렉트 시킴
 */
@Component
public class Oauth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final String OAUTH_FAIL_URI;

    public Oauth2FailureHandler(
            @Value("${url.base}") String BASE_URL,
            @Value("${path.oauth-fail}") String OAUTH_FAIL_PATH
    ){
        this.OAUTH_FAIL_URI = BASE_URL + OAUTH_FAIL_PATH;
    }

    @Override
    public void onAuthenticationFailure(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final AuthenticationException exception
    ) throws IOException, ServletException {
        final String redirectUri = UriComponentsBuilder.fromUriString(OAUTH_FAIL_URI)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(httpServletRequest, httpServletResponse, redirectUri);
    }
}
