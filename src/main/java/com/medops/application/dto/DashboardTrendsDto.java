package com.medops.application.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record DashboardTrendsDto(
    List<DailyTrendData> dailyTrends,
    List<TimeSlotData> timeSlotDistribution,
    List<TreatmentPopularityData> treatmentPopularity
) {
    
    @Builder
    public record DailyTrendData(
        LocalDate date,
        int totalReservations,
        int pendingReservations,
        int confirmedReservations,
        int canceledReservations,
        int completedReservations,
        long revenue
    ) {}
    
    @Builder
    public record TimeSlotData(
        String time,
        int count,
        long revenue
    ) {}
    
    @Builder
    public record TreatmentPopularityData(
        String treatmentName,
        int count,
        long revenue,
        double percentage
    ) {}
}