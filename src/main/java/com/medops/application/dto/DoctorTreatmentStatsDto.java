package com.medops.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record DoctorTreatmentStatsDto(
    String doctorId,
    String doctorName,
    List<TreatmentStatsDto> treatmentStats
) {
    
    @Builder
    public record TreatmentStatsDto(
        String treatmentName,
        int reservationCount,
        long revenue
    ) {}
}