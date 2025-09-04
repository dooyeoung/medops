package com.medops.adapter.in.web.exception;

import com.medops.common.error.ErrorCode;
import com.medops.common.response.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
@Order(2)
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<?> globalExceptionHandler(
        Exception exception,
        HttpServletRequest request
    ){
        log.error("", exception);
        
        // SSE 엔드포인트인지 확인 (Accept 헤더 또는 Content-Type 확인)
        String acceptHeader = request.getHeader("Accept");
        String contentType = request.getHeader("Content-Type");
        
        if ((acceptHeader != null && acceptHeader.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) ||
            (contentType != null && contentType.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) ||
            request.getRequestURI().contains("/notifications/stream")) {
            
            // SSE 엔드포인트의 경우 단순 문자열 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("SSE connection error: " + exception.getMessage());
        }
        
        // 일반 REST API 엔드포인트의 경우 기존 Api wrapper 사용
        return ResponseEntity.status(
            ErrorCode.SERVER_ERROR.getErrorCode()
        ).body(
            Api.ERROR(ErrorCode.SERVER_ERROR, exception)
        );
    }
}