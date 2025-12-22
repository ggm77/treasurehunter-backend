package com.treasurehunter.treasurehunter.domain.auth.oauth2.dto;

import lombok.Getter;

@Getter
public class Oauth2RequestDto {
    private String code;
    private String provider;
}
