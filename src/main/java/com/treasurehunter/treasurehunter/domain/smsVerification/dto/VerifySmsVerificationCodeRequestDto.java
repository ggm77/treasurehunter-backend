package com.treasurehunter.treasurehunter.domain.smsVerification.dto;

import lombok.Getter;

@Getter
public class VerifySmsVerificationCodeRequestDto {
    private String phoneNumber;
    private String code;
}
