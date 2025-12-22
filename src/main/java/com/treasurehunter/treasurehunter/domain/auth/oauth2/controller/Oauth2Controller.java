package com.treasurehunter.treasurehunter.domain.auth.oauth2.controller;

import com.treasurehunter.treasurehunter.domain.auth.oauth2.dto.Oauth2RequestDto;
import com.treasurehunter.treasurehunter.domain.auth.oauth2.service.Oauth2Service;
import com.treasurehunter.treasurehunter.domain.auth.token.dto.TokenResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class Oauth2Controller {

    private final Oauth2Service oauth2Service;

    //프론트에서 idToken 받아서 oauth2완료하는 API
    @PostMapping("/api/v1/auth/oauth2")
    public ResponseEntity<TokenResponseDto> oauth2Login(
            @RequestBody final Oauth2RequestDto oauth2RequestDto
    ){
        return ResponseEntity.ok().body(oauth2Service.processOauth2(oauth2RequestDto));
    }
}
