package com.medops.application.dto;

import lombok.Builder;

@Builder
public record DoctorStatsDto(
    String doctorId,
    String doctorName,
    int totalReservations,
    int confirmedReservations,
    int pendingReservations,
    int canceledReservations,
    int completedReservations,
    long revenue,
    double confirmationRate
) {}