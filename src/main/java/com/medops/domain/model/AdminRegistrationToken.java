package com.medops.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AdminRegistrationToken {
    private String id;
    private String adminId;
    private String registrationToken;
    private LocalDateTime registrationTokenExpiresAt;
    private LocalDateTime registeredAt;

    public void completeRegister() {
        this.registeredAt = LocalDateTime.now();
    }
}
