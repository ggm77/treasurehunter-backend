package com.treasurehunter.treasurehunter.global.auth.apple.dto.auth;

import lombok.Getter;

@Getter
public class AppleAuthRequestDto {
    private String authorizationCode;
    private String idToken;
    private String firstName;
    private String lastName;
    private String email;
}
