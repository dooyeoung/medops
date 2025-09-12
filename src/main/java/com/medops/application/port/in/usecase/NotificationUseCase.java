package com.medops.application.port.in.usecase;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationUseCase {
    SseEmitter subscribe(String hospitalId);
}