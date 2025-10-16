package com.treasurehunter.treasurehunter.domain.smsVerification.dto;

import lombok.Getter;

@Getter
public class SendSmsVerificationCodeRequestDto {
    private String phoneNumber;
}
