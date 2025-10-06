package com.treasurehunter.treasurehunter.domain.user.dto;

import lombok.Getter;

@Getter
public class UserRequestDto {
    private String uid;
    private String oauth;
    private String nickname;
    private String profileImage;
    private String name;
}
