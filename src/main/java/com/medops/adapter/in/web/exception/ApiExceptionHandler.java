package com.medops.adapter.in.web.exception;


import com.medops.common.error.ErrorCode;
import com.medops.common.response.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@Order(1)
public class ApiExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Api> apiExceptionHandler(Exception exception) {
        return ResponseEntity.status(
            ErrorCode.BAD_REQUEST.getErrorCode()
        ).body(
            Api.ERROR(ErrorCode.BAD_REQUEST, exception)
        );
    }
}
