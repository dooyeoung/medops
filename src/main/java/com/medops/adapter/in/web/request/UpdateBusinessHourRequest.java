package com.medops.adapter.in.web.request;

import java.time.LocalTime;

public record UpdateBusinessHourRequest(
    LocalTime openTime, LocalTime closeTime, LocalTime breakStartTime, LocalTime breakEndTime, Boolean closed
) {
}
