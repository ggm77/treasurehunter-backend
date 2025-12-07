package com.treasurehunter.treasurehunter.global.auth.apple.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppleAuthRequestDto {
    private String code;
    private String id_token;
    private String state;
    private String user;
}
