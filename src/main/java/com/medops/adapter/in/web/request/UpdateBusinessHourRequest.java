package com.medops.adapter.in.web.request;

public record UpdateBusinessHourRequest(
    String openTime, String closeTime, String breakStartTime, String breakEndTime, Boolean closed
) {
}
