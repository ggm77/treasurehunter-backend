package com.treasurehunter.treasurehunter.domain.smsVerification.controller;

import com.treasurehunter.treasurehunter.domain.smsVerification.dto.SendSmsVerificationCodeRequestDto;
import com.treasurehunter.treasurehunter.domain.smsVerification.dto.VerifySmsVerificationCodeRequestDto;
import com.treasurehunter.treasurehunter.domain.smsVerification.service.SendSmsVerificationCodeService;
import com.treasurehunter.treasurehunter.domain.smsVerification.service.VerifySmsVerificationCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SmsVerificationCodeController {

    private final SendSmsVerificationCodeService sendSmsVerificationCodeService;
    private final VerifySmsVerificationCodeService verifySmsVerificationCodeService;

    @PostMapping(value = "/sms/verification/code")
    public ResponseEntity<?> requestSmsVerification(
            @AuthenticationPrincipal final String userIdStr,
            @RequestBody final SendSmsVerificationCodeRequestDto sendSmsVerificationCodeRequestDto
    ){

        final Long userId = Long.parseLong(userIdStr);

        sendSmsVerificationCodeService.createVerificationCode(sendSmsVerificationCodeRequestDto, userId);

        return ResponseEntity.noContent().build();
    }


    @PostMapping(value = "/sms/verification/verify")
    public ResponseEntity<?> verifySmsVerification(
            @AuthenticationPrincipal final String userIdStr,
            @RequestBody final VerifySmsVerificationCodeRequestDto verifySmsVerificationCodeRequestDto
    ){
        final Long userId = Long.parseLong(userIdStr);

        verifySmsVerificationCodeService.verifyVerificationCode(verifySmsVerificationCodeRequestDto, userId);

        return ResponseEntity.noContent().build();
    }
}
