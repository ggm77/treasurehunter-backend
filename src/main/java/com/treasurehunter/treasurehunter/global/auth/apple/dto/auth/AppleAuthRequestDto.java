package com.treasurehunter.treasurehunter.global.auth.apple.dto.auth;

import lombok.Getter;

@Getter
public class AppleAuthRequestDto {
    private String code;
    private String id_token;
    private String state;
    private String user;
}
