package com.medops.application.port.in.command;

import java.time.LocalTime;

public record UpdateBusinessHourCommand (
    String businessHourId, LocalTime openTime, LocalTime closeTime, LocalTime breakStartTime, LocalTime breakEndTime, Boolean closed) {
}
