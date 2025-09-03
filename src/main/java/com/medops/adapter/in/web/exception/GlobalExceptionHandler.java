package com.medops.adapter.in.web.exception;

import com.medops.common.error.ErrorCode;
import com.medops.common.response.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Order(2)
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Api> globalExceptionHandler(
        Exception exception
    ){
        log.error("", exception);
        return ResponseEntity.status(
            ErrorCode.SERVER_ERROR.getErrorCode()
        ).body(
            Api.ERROR(ErrorCode.SERVER_ERROR, exception)
        );
    }
}