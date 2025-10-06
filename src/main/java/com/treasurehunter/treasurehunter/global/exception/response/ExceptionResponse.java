package com.treasurehunter.treasurehunter.global.exception.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class ExceptionResponse {
    private final LocalDateTime timestamp;
    private final HttpStatus status;
    private final String message;

    public ExceptionResponse(
            final HttpStatus status,
            final String message
    ) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
    }
}
