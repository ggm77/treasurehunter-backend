package com.treasurehunter.treasurehunter.domain.smsVerification.dto;

import lombok.Getter;

@Getter
public class VerifySmsVerificationRequestDto {
    private String e164;
    private String code;
}
