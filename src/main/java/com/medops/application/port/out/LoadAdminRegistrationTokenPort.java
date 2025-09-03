package com.medops.application.port.out;

import com.medops.domain.model.AdminRegistrationToken;

import java.util.Optional;

public interface LoadAdminRegistrationTokenPort {
    Optional<AdminRegistrationToken> loadAdminRegistrationTokenByAdminId(String adminId);
}
