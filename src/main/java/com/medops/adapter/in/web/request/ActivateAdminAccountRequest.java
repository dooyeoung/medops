package com.medops.adapter.in.web.request;

public record ActivateAdminAccountRequest(String adminEmail, String adminName, String password, String registrationToken) {
}
