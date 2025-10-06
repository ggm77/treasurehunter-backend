package com.treasurehunter.treasurehunter.global.exception.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExceptionCode {
    NICKNAME_DUPLICATE(HttpStatus.BAD_REQUEST, "닉네임이 이미 존재합니다."),
    AUTHENTICATION_ERROR(HttpStatus.UNAUTHORIZED, "인증 되지 않았습니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
