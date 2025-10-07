package com.treasurehunter.treasurehunter.global.exception.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExceptionCode {
    NICKNAME_DUPLICATE(HttpStatus.BAD_REQUEST, "닉네임이 이미 존재합니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 정보가 잘못되어 있습니다."),
    USER_NOT_EXIST(HttpStatus.BAD_REQUEST, "유저가 존재하지 않습니다."),
    AUTHENTICATION_ERROR(HttpStatus.UNAUTHORIZED, "인증 되지 않았습니다."),
    FORBIDDEN_USER_RESOURCE_ACCESS(HttpStatus.FORBIDDEN, "해당 정보에 접근할 수 없습니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
