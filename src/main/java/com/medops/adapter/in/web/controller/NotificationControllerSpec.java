package com.medops.adapter.in.web.controller;

import com.medops.domain.model.Admin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Tag(name = "알림 API", description = "실시간 알림 관련 API")
public interface NotificationControllerSpec {

    @Operation(
        summary = "SSE 알림 구독",
        description = "관리자가 실시간 알림을 받기 위한 Server-Sent Events 연결을 설정합니다. " +
                     "병원별로 연결이 관리되며, 해당 병원의 예약 변경 사항 등을 실시간으로 알림받을 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "SSE 연결 성공",
            content = @Content(
                mediaType = "text/event-stream",
                schema = @Schema(type = "string", example = "event: message\ndata: {\"type\":\"RESERVATION_UPDATE\",\"message\":\"새로운 예약이 등록되었습니다.\"}\n\n")
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "인증되지 않은 관리자"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "권한이 없는 사용자 (관리자가 아님)"
        )
    })
    SseEmitter subscribeNotifications(Admin admin);

    @Operation(
        summary = "SSE 연결 상태 조회",
        description = "현재 활성화된 SSE 연결 상태를 조회합니다. 모니터링 및 디버깅 목적으로 사용됩니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "연결 상태 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = "object",
                    example = "{\"totalConnections\": 5, \"activeHospitals\": 3}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "인증되지 않은 관리자"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "권한이 없는 사용자 (관리자가 아님)"
        )
    })
    Map<String, Integer> getConnectionStatus(Admin admin);
}