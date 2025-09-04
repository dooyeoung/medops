package com.medops.adapter.in.web.controller;

import com.medops.adapter.in.annotation.AdminSession;
import com.medops.application.service.SseEmitterService;
import com.medops.domain.model.Admin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SseEmitterService sseEmitterService;

    /**
     * 관리자용 SSE 연결 엔드포인트
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeNotifications(@AdminSession Admin admin) {
        log.info("SSE 구독 요청: adminId={}, hospitalId={}", admin.getId(), admin.getHospital().getId());
        
        return sseEmitterService.subscribe(admin.getHospital().getId());
    }

    /**
     * SSE 연결 상태 조회 (모니터링용)
     */
    @GetMapping("/status")
    public Map<String, Integer> getConnectionStatus(@AdminSession Admin admin) {
        return sseEmitterService.getConnectionStatus();
    }
}