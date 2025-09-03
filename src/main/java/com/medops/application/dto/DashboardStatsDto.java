package com.medops.application.dto;

import lombok.Builder;

@Builder
public record DashboardStatsDto(
    TodayStats today,
    MonthStats thisMonth,
    PerformanceStats performance
) {
    
    @Builder
    public record TodayStats(
        int total,
        int pending,
        int confirmed,
        int canceled,
        int completed,
        long revenue
    ) {}
    
    @Builder
    public record MonthStats(
        int totalBookings,
        int completedTreatments,
        long revenue,
        int newCustomers,
        int returningCustomers
    ) {}
    
    @Builder
    public record PerformanceStats(
        double confirmationRate,
        double cancellationRate,
        double avgDailyBookings,
        double noShowRate
    ) {}
}