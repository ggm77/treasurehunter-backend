package com.treasurehunter.treasurehunter.global.auth.apple.controller;

import com.treasurehunter.treasurehunter.domain.user.entity.Role;
import com.treasurehunter.treasurehunter.global.auth.apple.dto.auth.AppleAuthRequestDto;
import com.treasurehunter.treasurehunter.global.auth.apple.dto.auth.AppleAuthResponseDto;
import com.treasurehunter.treasurehunter.global.auth.apple.service.AppleAuthService;
import com.treasurehunter.treasurehunter.global.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AppleAuthController {

    private final AppleAuthService appleAuthService;
    private final CookieUtil cookieUtil;

    //애플 로그인 하는 API
    @PostMapping("/oauth2/authorization/apple")
    public ResponseEntity<Void> appleAuth(
            @RequestBody final AppleAuthRequestDto appleAuthRequestDto
    ) {
        // 1) 요청 받은 정보 토대로 애플 인증 진행
        final AppleAuthResponseDto appleAuthResponseDto = appleAuthService.processAppleAuth(appleAuthRequestDto);

        // 2) 인증 후 가져온 정보들 변수에 저장
        final String accessToken = appleAuthResponseDto.getAccessToken();
        final String refreshToken = appleAuthResponseDto.getRefreshToken();
        final Role role = appleAuthResponseDto.getRole();
        final Long userId = appleAuthResponseDto.getUserId();

        // 3) JWT 담은 쿠키 생성
        final Map<String, ResponseCookie> jwtCookie = cookieUtil.createJwtCookie(accessToken, refreshToken);

        // 4) 만든 쿠키 변수에 저장
        final ResponseCookie accessTokenCookie = jwtCookie.get("accessToken");
        final ResponseCookie refreshTokenCookie = jwtCookie.get("refreshToken");

        // 5) 헤더 만들고 헤더에 쿠키 저장
        final HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        httpHeaders.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        // 6) 헤더에 리다이렉트 될 주소 지정
        httpHeaders.setLocation(URI.create(cookieUtil.getRedirectUriByRole(role, userId)));

        return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
    }
}
