package com.medops.application.port.in.command;

import com.medops.domain.model.Admin;

public record UpdateAdminPasswordCommand(
    Admin admin,
    String currentPassword,
    String newPassword
) {
}
