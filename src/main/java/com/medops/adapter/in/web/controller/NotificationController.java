package com.medops.adapter.in.web.controller;

import com.medops.adapter.in.annotation.AdminSession;
import com.medops.application.port.in.usecase.NotificationUseCase;
import com.medops.domain.model.Admin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationControllerSpec {

    private final NotificationUseCase notificationUseCase;

    @Override
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeNotifications(@AdminSession Admin admin) {
        return notificationUseCase.subscribe(admin.getHospital().getId());
    }

}