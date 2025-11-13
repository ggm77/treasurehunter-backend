package com.treasurehunter.treasurehunter.global.stomp.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Getter
public class StompAuthResultDto {
    private final String userIdStr;
    private final List<SimpleGrantedAuthority> authorities;
    private final long exp;

    @Builder
    public StompAuthResultDto(
            final String userIdStr,
            final List<SimpleGrantedAuthority> authorities,
            final long exp
    ) {
        this.userIdStr = userIdStr;
        this.authorities = authorities;
        this.exp = exp;
    }
}
