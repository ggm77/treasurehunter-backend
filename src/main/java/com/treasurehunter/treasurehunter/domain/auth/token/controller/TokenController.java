package com.treasurehunter.treasurehunter.domain.auth.token.controller;

import com.treasurehunter.treasurehunter.domain.auth.token.dto.TokenResponseDto;
import com.treasurehunter.treasurehunter.domain.auth.token.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TokenController {

    private final TokenService tokenService;

    //쿠키에 있는 엑세스 토큰, 리프레시 토큰 가져오는 API
    @GetMapping("/auth/token")
    public ResponseEntity<TokenResponseDto> getToken(final HttpServletRequest httpServletRequest){

        final TokenResponseDto tokenResponseDto = tokenService.getToken(httpServletRequest);

        return ResponseEntity.ok(tokenResponseDto);
    }
}
