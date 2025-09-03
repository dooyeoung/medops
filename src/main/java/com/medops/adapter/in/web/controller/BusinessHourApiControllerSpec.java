package com.medops.adapter.in.web.controller;

import com.medops.adapter.in.web.request.UpdateBusinessHourRequest;
import com.medops.common.response.Api;
import com.medops.domain.model.BusinessHour;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Null;

import java.util.List;

@Tag(name = "영업시간 API", description = "병원 영업시간 관리 API")
public interface BusinessHourApiControllerSpec {

    @Operation(
        summary = "병원 영업시간 조회",
        description = "특정 병원의 모든 요일별 영업시간을 조회합니다.",
        parameters = {
            @Parameter(
                name = "hospitalId",
                description = "병원 ID",
                required = true,
                example = "hospital-123"
            )
        }
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "영업시간 조회 성공",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = BusinessHour.class))
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "병원을 찾을 수 없음"
        )
    })
    Api<List<BusinessHour>> getBusinessHours(String hospitalId);

    @Operation(
        summary = "영업시간 수정",
        description = "특정 영업시간 설정을 수정합니다. 영업시간, 휴게시간, 휴무일 여부를 변경할 수 있습니다.",
        parameters = {
            @Parameter(
                name = "businessHourId",
                description = "수정할 영업시간 ID",
                required = true,
                example = "business-hour-123"
            )
        },
        requestBody = @RequestBody(
            description = "영업시간 수정 요청",
            required = true,
            content = {
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UpdateBusinessHourRequest.class)
                )
            }
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "영업시간 수정 성공"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "영업시간 설정을 찾을 수 없음"
        )
    })
    Api<Null> updateBusinessHour(String businessHourId, UpdateBusinessHourRequest request);
}