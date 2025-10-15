package com.treasurehunter.treasurehunter.domain.smsVerification.controller;

import com.treasurehunter.treasurehunter.domain.smsVerification.dto.RequestSmsVerificationRequestDto;
import com.treasurehunter.treasurehunter.domain.smsVerification.dto.VerifySmsVerificationRequestDto;
import com.treasurehunter.treasurehunter.domain.smsVerification.service.SmsVerificationService;
import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SmsVerificationController {

    private final SmsVerificationService smsVerificationService;
    private final JwtProvider jwtProvider;

    @PostMapping(value = "/sms-verification/request")
    public ResponseEntity<?> requestSmsVerification(
            @RequestHeader(value = "Authorization") final String token,
            @RequestBody final RequestSmsVerificationRequestDto requestSmsVerificationRequestDto
    ){

        final Long userId = Long.parseLong(jwtProvider.validateToken(token.substring(7)));

        smsVerificationService.createVerificationCode(requestSmsVerificationRequestDto);

        return ResponseEntity.noContent().build();
    }
}
