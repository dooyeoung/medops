package com.medops.adapter.in.web.controller;


import com.medops.adapter.in.web.request.HospitalCreateRequest;
import com.medops.application.dto.HospitalWithProductsDto;
import com.medops.common.response.Api;
import com.medops.domain.model.Hospital;
import com.medops.domain.model.TreatmentProduct;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "병원 API")
public interface HospitalApiControllerSpec {

    @Operation(
        summary = "병원 등록",
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = HospitalCreateRequest.class)
            )
        )
    )
    Api<Hospital> createHospital(
        HospitalCreateRequest request
    );


    @Operation(
        summary = "병원 목록",
        requestBody = @RequestBody(
            content = {
                @Content(
                    mediaType = "application/json"
                )
            }
        )
    )
    Api<List<HospitalWithProductsDto>> getAllHospitals();

    @Operation(
        summary = "병원 시술 상품 목록 조회",
        description = "특정 병원의 모든 시술 상품을 조회합니다.",
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
            description = "시술 상품 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = TreatmentProduct.class))
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "병원을 찾을 수 없음"
        )
    })
    Api<List<TreatmentProduct>> getHospitalTreatmentProducts(String hospitalId);
}
