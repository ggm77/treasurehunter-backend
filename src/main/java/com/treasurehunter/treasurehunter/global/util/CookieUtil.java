package com.treasurehunter.treasurehunter.global.util;

import com.treasurehunter.treasurehunter.domain.user.entity.Role;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

@Component
public class CookieUtil {

    @Value("${server.isHttps}")
    private Boolean isHttps;

    @Value("${jwt.cookie.domain}")
    private String jwtCookieDomain;

    @Value("${jwt.accessToken.exprTime}")
    private Long accessTokenExpireTime;

    @Value("${jwt.refreshToken.exprTime}")
    private Long refreshTokenExpireTime;

    @Value("${url.base}")
    private String BASE_URL;

    @Value("${path.new-user}")
    private String NEW_USER_PATH;

    @Value("${path.existing-user}")
    private String EXISTING_USER_PATH;

    /**
     * HttpServletRequest에서 쿠키를 찾는 메서드
     * 쿠키 이름 중에 같은 걸 찾아서 쿠키 값을 리턴해줌
     * @param httpServletRequest
     * @param cookieName
     * @return 찾는 쿠키의 값
     */
    public String getCookieValue(
            final HttpServletRequest httpServletRequest,
            final String cookieName
    ){
        if(httpServletRequest.getCookies() == null){
            return null;
        }

        return Arrays.stream(httpServletRequest.getCookies())
                .filter(c -> cookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * JWT를 넣은 쿠키를 만드는 메서드
     * @param accessToken 엑세스 토큰
     * @param refreshToken 리프리시 토큰
     * @return Map 형태로 쿠키 두개를 리턴함
     */
    public Map<String, ResponseCookie> createJwtCookie(
            final String accessToken,
            final String refreshToken
    ){
        final ResponseCookie accessTokenCookie;
        final ResponseCookie refreshTokenCookie;

        //https일 경우 쿠키세팅
        if(isHttps) {
            //엑세스 토큰 담은 쿠키 생성
            accessTokenCookie = ResponseCookie.from("ACCESS_TOKEN", accessToken)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    //프론트가 서버와 같은 Url에서 배포 된다면 주석 풀기
//                    .partitioned(true)
                    .domain(jwtCookieDomain)
                    .path("/")
                    .maxAge(Duration.ofSeconds(accessTokenExpireTime))
                    .build();

            //리프레시 토큰 담은 쿠키 생성
            refreshTokenCookie = ResponseCookie.from("REFRESH_TOKEN", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    //프론트가 서버와 같은 Url에서 배포 된다면 주석 풀기
//                    .partitioned(true)
                    .domain(jwtCookieDomain)
                    .path("/")
                    .maxAge(Duration.ofSeconds(refreshTokenExpireTime))
                    .build();
        } else {
            //엑세스 토큰 담은 쿠키 생성
            accessTokenCookie = ResponseCookie.from("ACCESS_TOKEN", accessToken)
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(Duration.ofSeconds(accessTokenExpireTime))
                    .build();

            //리프레시 토큰 담은 쿠키 생성
            refreshTokenCookie = ResponseCookie.from("REFRESH_TOKEN", refreshToken)
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(Duration.ofSeconds(refreshTokenExpireTime))
                    .build();
        }

        return Map.of(
                "accessToken", accessTokenCookie,
                "refreshToken", refreshTokenCookie
        );
    }

    /**
     * 유저 role (신규 / 기존 유저)에 따라서 리다이렉트 URI설정
     * @param role 유저의 Role
     * @param userId 유저 ID
     * @return 리다이렉트 URI
     */
    public String getRedirectUriByRole(
            final Role role,
            final Long userId
    ){
        if(role.equals(Role.NOT_REGISTERED)){
            return UriComponentsBuilder.fromUriString(BASE_URL + NEW_USER_PATH)
                    .queryParam("userId", userId)
                    .build()
                    .toUriString();
        }
        return UriComponentsBuilder.fromUriString(BASE_URL + EXISTING_USER_PATH)
                .queryParam("userId", userId)
                .build()
                .toUriString();
    }
}
