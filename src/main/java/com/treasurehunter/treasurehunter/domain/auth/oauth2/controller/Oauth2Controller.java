package com.treasurehunter.treasurehunter.domain.auth.oauth2.controller;

import com.treasurehunter.treasurehunter.domain.auth.oauth2.dto.Oauth2RequestDto;
import com.treasurehunter.treasurehunter.domain.auth.oauth2.dto.Oauth2ResponseDto;
import com.treasurehunter.treasurehunter.domain.auth.oauth2.service.Oauth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class Oauth2Controller {

    private final Oauth2Service oauth2Service;

    //프론트에서 authorization code 받아서 oauth2완료하는 API
    @PostMapping("/api/v1/auth/oauth2")
    public ResponseEntity<Oauth2ResponseDto> oauth2Login(
            @RequestBody final Oauth2RequestDto oauth2RequestDto
    ){
        return ResponseEntity.ok().body(oauth2Service.processOauth2(oauth2RequestDto));
    }

    // 구글에서 OAuth 이벤트 발생시 요청 보내는 API
    @PostMapping("/api/v1/auth/google/risc")
    public ResponseEntity<Void> handleRiscEvent(
            @RequestBody final String securityEventToken
    ) {
        oauth2Service.handleGoogleRiscEvent(securityEventToken);
        return ResponseEntity.noContent().build();
    }

    // 애플에서 OAuth 이벤트 발생시 요청 보내는 API
    @PostMapping("/api/v1/auth/apple/sps")
    public ResponseEntity<Void> handleAppleSps(
            @RequestParam("payload") String payload
    ) {
        oauth2Service.handleAppleSpsEvent(payload);
        return ResponseEntity.noContent().build();
    }
}
