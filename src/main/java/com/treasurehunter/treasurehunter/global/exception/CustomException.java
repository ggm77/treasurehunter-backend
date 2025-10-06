package com.treasurehunter.treasurehunter.global.exception;

import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ExceptionCode exceptionCode;

    public CustomException(ExceptionCode exceptionCode) { this.exceptionCode = exceptionCode; }
}
