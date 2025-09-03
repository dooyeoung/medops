package com.medops.adapter.in.web.request;

public record UpdateAdminPasswordRequest(String currentPassword, String newPassword) {
}
