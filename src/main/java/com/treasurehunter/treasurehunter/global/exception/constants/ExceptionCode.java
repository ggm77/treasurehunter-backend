package com.treasurehunter.treasurehunter.global.exception.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExceptionCode {
    MULTI_STATUS(HttpStatus.MULTI_STATUS, "multiple status changes"),
    TOKEN_NOT_FOUND_IN_COOKIE(HttpStatus.BAD_REQUEST, "쿠키에서 토큰을 찾을 수 없습니다."),
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "지원되지 않는 OAuth입니다."),
    NICKNAME_DUPLICATE(HttpStatus.BAD_REQUEST, "닉네임이 이미 존재합니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 정보가 잘못되어 있습니다."),
    USER_NOT_EXIST(HttpStatus.BAD_REQUEST, "유저가 존재하지 않습니다."),
    POST_NOT_EXIST(HttpStatus.BAD_REQUEST, "게시글을 찾을 수 없습니다."),
    USER_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "회원가입된 유저가 이미 존재 합니다."),
    CODE_NOT_EXIST(HttpStatus.BAD_REQUEST, "인증 코드가 없거나 만료되었습니다."),
    CODE_NOT_MATCH(HttpStatus.BAD_REQUEST, "인증 코드가 일치하지 않습니다."),
    SMS_SEND_FAILED(HttpStatus.BAD_REQUEST, "SMS 발송에 실패 했습니다."),
    POINT_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "포인트가 부족합니다."),
    POST_IS_COMPLETED(HttpStatus.BAD_REQUEST, "게시글이 완료 처리되어 있습니다."),
    FILE_NOT_UPLOADED(HttpStatus.BAD_REQUEST, "파일이 업로드 되지 않았습니다."),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "파일명이 올바르지 않습니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "파일 확장자가 올바르지 않습니다."),
    TOO_BIG_FILE(HttpStatus.BAD_REQUEST, "너무 큰 파일입니다."),
    INVALID_FILE(HttpStatus.BAD_REQUEST, "잘못된 파일입니다."),
    FILE_NOT_FOUND(HttpStatus.BAD_REQUEST, "파일을 찾을 수 없습니다."),
    POST_LIKE_NOT_EXIST(HttpStatus.BAD_REQUEST, "좋아요가 존재하지 않습니다."),
    POST_LIKE_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "좋아요를 이미 눌렀습니다."),
    POST_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "게시글이 완료되지 않았습니다."),
    REVIEW_NOT_EXIST(HttpStatus.BAD_REQUEST, "후기가 존재하지 않습니다."),
    BADGE_NOT_EXIST(HttpStatus.BAD_REQUEST, "뱃지 정보가 존재하지 않습니다."),
    BADGE_ALREADY_OWNED(HttpStatus.BAD_REQUEST, "뱃지를 유저가 소유하고 있기 때문에 삭제가 불가능합니다."),

    AUTHENTICATION_ERROR(HttpStatus.UNAUTHORIZED, "인증 되지 않았습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    FORBIDDEN_USER_RESOURCE_ACCESS(HttpStatus.FORBIDDEN, "해당 정보에 접근할 수 없습니다."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "해당 기능에 접근할 수 없습니다."),
    COOL_DOWN(HttpStatus.TOO_MANY_REQUESTS, "쿨다운 중 입니다."),
    DAILY_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "일일 요청가능한 횟수를 초과했습니다."),
    ATTEMPT_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "시도 가능한 횟수를 초과했습니다."),

    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패 했습니다."),
    FILE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장에 실패 했습니다."),
    FILE_NAME_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일명 생성에 실패했습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "현재 서버를 이용할 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 오류가 발생했습니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
