package com.medops.adapter.in.web.controller;

import com.medops.application.dto.DashboardStatsDto;
import com.medops.application.dto.DashboardTrendsDto;
import com.medops.application.dto.DoctorStatsDto;
import com.medops.application.dto.DoctorTreatmentStatsDto;
import com.medops.application.dto.HeatmapDto;
import com.medops.common.response.Api;
import com.medops.domain.model.Admin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "대시보드 API", description = "병원 관리자를 위한 대시보드 통계 및 분석 데이터 API")
public interface DashboardApiControllerSpec {

    @Operation(
        summary = "대시보드 기본 통계 조회",
        description = "병원의 오늘 예약 현황, 최근 30일 성과, 성능 지표를 포함한 기본 통계 데이터를 조회합니다.",
        parameters = {
            @Parameter(
                name = "hospitalId",
                description = "통계를 조회할 병원 ID",
                required = true,
                example = "hospital-123"
            )
        }
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "대시보드 통계 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DashboardStatsDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "관리자 인증 필요"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "해당 병원에 대한 접근 권한 없음"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "병원을 찾을 수 없음"
        )
    })
    Api<DashboardStatsDto> getDashboardStats(Admin admin, String hospitalId);

    @Operation(
        summary = "대시보드 트렌드 분석 조회",
        description = "지정된 기간 동안의 일별 예약 추이, 시간대별 분포, 시술 인기도를 포함한 트렌드 데이터를 조회합니다.",
        parameters = {
            @Parameter(
                name = "hospitalId",
                description = "통계를 조회할 병원 ID",
                required = true,
                example = "hospital-123"
            ),
            @Parameter(
                name = "days",
                description = "조회할 일수 (기본값: 7일)",
                required = false,
                example = "7"
            )
        }
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "트렌드 분석 데이터 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DashboardTrendsDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 days 파라미터 (1-365 범위 초과)"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "관리자 인증 필요"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "해당 병원에 대한 접근 권한 없음"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "병원을 찾을 수 없음"
        )
    })
    Api<DashboardTrendsDto> getDashboardTrends(Admin admin, String hospitalId, int days);

    @Operation(
        summary = "실시간 대시보드 요약 조회",
        description = "오늘의 실시간 예약 현황을 요약하여 제공합니다. 대시보드 메인 화면용 데이터입니다.",
        parameters = {
            @Parameter(
                name = "hospitalId",
                description = "통계를 조회할 병원 ID",
                required = true,
                example = "hospital-123"
            )
        }
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "실시간 요약 데이터 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DashboardStatsDto.TodayStats.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "관리자 인증 필요"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "해당 병원에 대한 접근 권한 없음"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "병원을 찾을 수 없음"
        )
    })
    Api<DashboardStatsDto.TodayStats> getRealTimeSummary(Admin admin, String hospitalId);

    @Operation(
        summary = "예약 히트맵 데이터 조회",
        description = "요일별(일~토) × 시간대별(7~22시) 예약 현황을 히트맵 형태로 제공합니다. 피크 시간대 분석에 활용됩니다.",
        parameters = {
            @Parameter(
                name = "hospitalId",
                description = "통계를 조회할 병원 ID",
                required = true,
                example = "hospital-123"
            ),
            @Parameter(
                name = "days",
                description = "조회할 일수 (기본값: 30일)",
                required = false,
                example = "30"
            )
        }
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "히트맵 데이터 조회 성공",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = HeatmapDto.class))
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 days 파라미터 (1-365 범위 초과)"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "관리자 인증 필요"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "해당 병원에 대한 접근 권한 없음"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "병원을 찾을 수 없음"
        )
    })
    Api<List<HeatmapDto>> getHeatmapData(Admin admin, String hospitalId, int days);

    @Operation(
        summary = "의사별 예약 통계 조회",
        description = "병원 소속 의사별 예약 현황, 확정률, 매출 등의 성과 통계를 제공합니다.",
        parameters = {
            @Parameter(
                name = "hospitalId",
                description = "통계를 조회할 병원 ID",
                required = true,
                example = "hospital-123"
            ),
            @Parameter(
                name = "days",
                description = "조회할 일수 (기본값: 7일)",
                required = false,
                example = "7"
            )
        }
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "의사별 통계 조회 성공",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = DoctorStatsDto.class))
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 days 파라미터 (1-365 범위 초과)"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "관리자 인증 필요"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "해당 병원에 대한 접근 권한 없음"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "병원을 찾을 수 없음"
        )
    })
    Api<List<DoctorStatsDto>> getDoctorStats(Admin admin, String hospitalId, int days);

    @Operation(
        summary = "의사별 시술 예약 통계 조회",
        description = "의사별-시술별 상세 예약 현황과 매출을 제공합니다. 모든 시술 상품을 포함하며, 의사별 전문 분야 분석에 활용됩니다.",
        parameters = {
            @Parameter(
                name = "hospitalId",
                description = "통계를 조회할 병원 ID",
                required = true,
                example = "hospital-123"
            ),
            @Parameter(
                name = "days",
                description = "조회할 일수 (기본값: 7일)",
                required = false,
                example = "7"
            )
        }
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "의사별 시술 통계 조회 성공",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = DoctorTreatmentStatsDto.class))
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 days 파라미터 (1-365 범위 초과)"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "관리자 인증 필요"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "해당 병원에 대한 접근 권한 없음"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "병원을 찾을 수 없음"
        )
    })
    Api<List<DoctorTreatmentStatsDto>> getDoctorTreatmentStats(Admin admin, String hospitalId, int days);
}