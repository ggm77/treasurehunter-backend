package com.treasurehunter.treasurehunter.domain.smsVerification.controller;

import com.treasurehunter.treasurehunter.domain.smsVerification.dto.SendSmsVerificationCodeRequestDto;
import com.treasurehunter.treasurehunter.domain.smsVerification.dto.VerifySmsVerificationCodeRequestDto;
import com.treasurehunter.treasurehunter.domain.smsVerification.service.SendSmsVerificationCodeService;
import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SmsVerificationCodeController {

    private final SendSmsVerificationCodeService sendSmsVerificationCodeService;
    private final JwtProvider jwtProvider;

    @PostMapping(value = "/sms/verification/code")
    public ResponseEntity<?> requestSmsVerification(
            @RequestHeader(value = "Authorization") final String token,
            @RequestBody final SendSmsVerificationCodeRequestDto sendSmsVerificationCodeRequestDto
    ){

        jwtProvider.validateToken(token.substring(7));

        sendSmsVerificationCodeService.createVerificationCode(sendSmsVerificationCodeRequestDto);

        return ResponseEntity.noContent().build();
    }
}
