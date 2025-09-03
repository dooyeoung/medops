package com.medops.application.port.in.usecase;

import com.medops.application.dto.DashboardStatsDto;
import com.medops.application.dto.DashboardTrendsDto;
import com.medops.application.dto.DoctorStatsDto;
import com.medops.application.dto.DoctorTreatmentStatsDto;
import com.medops.application.dto.HeatmapDto;

import java.util.List;

public interface DashboardUseCase {
    
    /**
     * 대시보드 기본 통계 데이터 조회
     * @param hospitalId 병원 ID
     * @return 오늘, 이번달, 성과 지표
     */
    DashboardStatsDto getDashboardStats(String hospitalId);
    
    /**
     * 대시보드 트렌드 분석 데이터 조회
     * @param hospitalId 병원 ID
     * @param days 조회할 일수 (기본 7일)
     * @return 일별, 시간대별, 시술별 트렌드
     */
    DashboardTrendsDto getDashboardTrends(String hospitalId, int days);
    
    /**
     * 실시간 요약 정보
     * @param hospitalId 병원 ID
     * @return 오늘 현재 상태
     */
    DashboardStatsDto.TodayStats getRealTimeSummary(String hospitalId);
    
    /**
     * 히트맵 데이터 조회 (요일별/시간대별 예약 현황)
     * @param hospitalId 병원 ID
     * @param days 조회할 기간 (일수)
     * @return 요일별 시간대별 예약 건수 데이터
     */
    List<HeatmapDto> getHeatmapData(String hospitalId, int days);
    
    /**
     * 의사별 예약 통계 조회
     * @param hospitalId 병원 ID
     * @param days 조회할 기간 (일수)
     * @return 의사별 예약 현황 통계
     */
    List<DoctorStatsDto> getDoctorStats(String hospitalId, int days);
    
    /**
     * 의사별 시술 예약 통계 조회
     * @param hospitalId 병원 ID
     * @param days 조회할 기간 (일수)
     * @return 의사별-시술별 예약 현황 통계
     */
    List<DoctorTreatmentStatsDto> getDoctorTreatmentStats(String hospitalId, int days);
}