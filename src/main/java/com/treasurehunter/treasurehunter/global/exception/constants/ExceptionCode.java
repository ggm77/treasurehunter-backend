package com.treasurehunter.treasurehunter.global.exception.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExceptionCode {
    TOKEN_NOT_FOUND_IN_COOKIE(HttpStatus.BAD_REQUEST, "쿠키에서 토큰을 찾을 수 없습니다."),
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "지원되지 않는 OAuth입니다."),
    NICKNAME_DUPLICATE(HttpStatus.BAD_REQUEST, "닉네임이 이미 존재합니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 정보가 잘못되어 있습니다."),
    USER_NOT_EXIST(HttpStatus.BAD_REQUEST, "유저가 존재하지 않습니다."),
    USER_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "회원가입된 유저가 이미 존재 합니다."),
    CODE_NOT_EXIST(HttpStatus.BAD_REQUEST, "인증 코드가 없거나 만료되었습니다."),
    CODE_NOT_MATCH(HttpStatus.BAD_REQUEST, "인증 코드가 일치하지 않습니다."),
    SMS_SEND_FAILED(HttpStatus.BAD_REQUEST, "SMS 발송에 실패 했습니다."),
    AUTHENTICATION_ERROR(HttpStatus.UNAUTHORIZED, "인증 되지 않았습니다."),
    FORBIDDEN_USER_RESOURCE_ACCESS(HttpStatus.FORBIDDEN, "해당 정보에 접근할 수 없습니다."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "해당 기능에 접근할 수 없습니다."),
    COOL_DOWN(HttpStatus.TOO_MANY_REQUESTS, "쿨다운 중 입니다."),
    DAILY_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "일일 요청가능한 횟수를 초과했습니다."),
    ATTEMPT_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "시도 가능한 횟수를 초과했습니다."),

    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "현재 서버를 이용할 수 없습니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
