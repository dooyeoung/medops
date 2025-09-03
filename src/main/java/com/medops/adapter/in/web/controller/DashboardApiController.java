package com.medops.adapter.in.web.controller;

import com.medops.adapter.in.annotation.AdminSession;
import com.medops.application.dto.DashboardStatsDto;
import com.medops.application.dto.DashboardTrendsDto;
import com.medops.application.dto.DoctorStatsDto;
import com.medops.application.dto.DoctorTreatmentStatsDto;
import com.medops.application.dto.HeatmapDto;
import com.medops.application.port.in.usecase.DashboardUseCase;
import com.medops.common.response.Api;
import com.medops.domain.model.Admin;

import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardApiController implements DashboardApiControllerSpec {
    
    private final DashboardUseCase dashboardUseCase;

    @GetMapping("/stats/{hospitalId}")
    public Api<DashboardStatsDto> getDashboardStats(
        @Parameter(hidden = true) @AdminSession Admin admin,
        @PathVariable String hospitalId
    ) {
        DashboardStatsDto stats = dashboardUseCase.getDashboardStats(hospitalId);
        return Api.OK(stats);
    }

    @GetMapping("/trends/{hospitalId}")
    public Api<DashboardTrendsDto> getDashboardTrends(
        @Parameter(hidden = true) @AdminSession Admin admin,
        @PathVariable String hospitalId,
        @RequestParam(defaultValue = "7") int days
    ) {
        DashboardTrendsDto trends = dashboardUseCase.getDashboardTrends(hospitalId, days);
        return Api.OK(trends);
    }

    @GetMapping("/summary/{hospitalId}")
    public Api<DashboardStatsDto.TodayStats> getRealTimeSummary(
        @Parameter(hidden = true) @AdminSession Admin admin,
        @PathVariable String hospitalId
    ) {
        DashboardStatsDto.TodayStats summary = dashboardUseCase.getRealTimeSummary(hospitalId);
        return Api.OK(summary);
    }

    @GetMapping("/heatmap/{hospitalId}")
    public Api<List<HeatmapDto>> getHeatmapData(
        @Parameter(hidden = true) @AdminSession Admin admin,
        @PathVariable String hospitalId,
        @RequestParam(defaultValue = "30") int days
    ) {
        List<HeatmapDto> heatmapData = dashboardUseCase.getHeatmapData(hospitalId, days);
        return Api.OK(heatmapData);
    }

    @GetMapping("/doctor-stats/{hospitalId}")
    public Api<List<DoctorStatsDto>> getDoctorStats(
        @Parameter(hidden = true) @AdminSession Admin admin,
        @PathVariable String hospitalId,
        @RequestParam(defaultValue = "7") int days
    ) {
        List<DoctorStatsDto> doctorStats = dashboardUseCase.getDoctorStats(hospitalId, days);
        return Api.OK(doctorStats);
    }

    @GetMapping("/doctor-treatment-stats/{hospitalId}")
    public Api<List<DoctorTreatmentStatsDto>> getDoctorTreatmentStats(
        @Parameter(hidden = true) @AdminSession Admin admin,
        @PathVariable String hospitalId,
        @RequestParam(defaultValue = "7") int days
    ) {
        List<DoctorTreatmentStatsDto> doctorTreatmentStats = dashboardUseCase.getDoctorTreatmentStats(hospitalId, days);
        return Api.OK(doctorTreatmentStats);
    }
}