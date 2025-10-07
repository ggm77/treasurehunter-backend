package com.treasurehunter.treasurehunter.global.exception.handler;

import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.response.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public final ResponseEntity<ExceptionResponse> handleException(final CustomException ex) {

        final ExceptionResponse exceptionResponse = new ExceptionResponse(
                ex.getExceptionCode().getStatus(),
                ex.getExceptionCode().getMessage()
        );

        return new ResponseEntity<>(exceptionResponse, ex.getExceptionCode().getStatus());
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ExceptionResponse> handleAllException(final Exception ex) {
        final ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error"
        );

        log.error("CustomizedResponseEntityExceptionHandler.handleAllExceptions message:{}", ex.getMessage(), ex);

        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
