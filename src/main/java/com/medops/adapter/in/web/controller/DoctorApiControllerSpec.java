package com.medops.adapter.in.web.controller;

import com.medops.adapter.in.web.request.CreateDoctorRequest;
import com.medops.adapter.in.web.request.UpdateDoctorRequest;
import com.medops.common.response.Api;
import com.medops.domain.model.Doctor;
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

@Tag(name = "의사 API", description = "병원 의사 관리 API")
public interface DoctorApiControllerSpec {

    @Operation(
        summary = "병원 의사 목록 조회",
        description = "특정 병원에 소속된 모든 의사의 목록을 조회합니다.",
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
            description = "의사 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = Doctor.class))
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "병원을 찾을 수 없음"
        )
    })
    Api<List<Doctor>> getDoctorsByHospitalId(String hospitalId);

    @Operation(
        summary = "의사 등록",
        description = "새로운 의사를 병원에 등록합니다.",
        requestBody = @RequestBody(
            description = "의사 등록 요청",
            required = true,
            content = {
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateDoctorRequest.class)
                )
            }
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "의사 등록 성공"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "병원을 찾을 수 없음"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "이미 존재하는 의사"
        )
    })
    Api<Null> createDoctor(CreateDoctorRequest request);

    @Operation(
        summary = "의사 정보 수정",
        description = "기존 의사의 정보를 수정합니다.",
        parameters = {
            @Parameter(
                name = "doctorId",
                description = "수정할 의사의 ID",
                required = true,
                example = "doctor-123"
            )
        },
        requestBody = @RequestBody(
            description = "의사 정보 수정 요청",
            required = true,
            content = {
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UpdateDoctorRequest.class)
                )
            }
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "의사 정보 수정 성공"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "의사를 찾을 수 없음"
        )
    })
    Api<Null> updateDoctor(String doctorId, UpdateDoctorRequest request);
}