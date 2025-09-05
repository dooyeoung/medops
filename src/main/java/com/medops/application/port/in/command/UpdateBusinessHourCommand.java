package com.medops.application.port.in.command;

public record UpdateBusinessHourCommand (
    String businessHourId, String openTime, String closeTime, String breakStartTime, String breakEndTime, Boolean closed) {
}
