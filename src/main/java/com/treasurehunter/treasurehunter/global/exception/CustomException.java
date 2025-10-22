package com.treasurehunter.treasurehunter.global.exception;

import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ExceptionCode exceptionCode;

    public CustomException(ExceptionCode exceptionCode) {
        super("");
        this.exceptionCode = exceptionCode;
    }
    public CustomException(ExceptionCode exceptionCode, String message) {
        super(message);
        this.exceptionCode = exceptionCode;
    }
}
