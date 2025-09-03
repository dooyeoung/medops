package com.medops.adapter.in.web.controller;

import com.medops.adapter.in.web.request.CreateTreatmentProductRequest;
import com.medops.adapter.in.web.request.UpdateTreatmentProductRequest;
import com.medops.common.response.Api;
import com.medops.domain.model.TreatmentProduct;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Null;

@Tag(name = "시술 상품 API", description = "병원 시술 상품 관리 API")
public interface TreatmentProductApiControllerSpec {

    @Operation(
        summary = "시술 상품 생성",
        description = "새로운 시술 상품을 생성합니다. 시술명, 설명, 최대 수용 인원, 가격을 설정할 수 있습니다.",
        requestBody = @RequestBody(
            description = "시술 상품 생성 요청",
            required = true,
            content = {
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateTreatmentProductRequest.class)
                )
            }
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "시술 상품 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TreatmentProduct.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터 (필수 필드 누락, 잘못된 가격 형식 등)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "병원을 찾을 수 없음"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "동일한 이름의 시술 상품이 이미 존재함"
        )
    })
    Api<TreatmentProduct> createTreatmentProduct(CreateTreatmentProductRequest request);

    @Operation(
        summary = "시술 상품 정보 수정",
        description = "기존 시술 상품의 정보를 수정합니다. 시술명, 설명, 최대 수용 인원, 가격을 변경할 수 있습니다.",
        parameters = {
            @Parameter(
                name = "treatmentProductId",
                description = "수정할 시술 상품 ID",
                required = true,
                example = "product-123"
            )
        },
        requestBody = @RequestBody(
            description = "시술 상품 수정 요청",
            required = true,
            content = {
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UpdateTreatmentProductRequest.class)
                )
            }
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "시술 상품 수정 성공"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터 (필수 필드 누락, 잘못된 가격 형식 등)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "시술 상품을 찾을 수 없음"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "동일한 이름의 시술 상품이 이미 존재함"
        )
    })
    Api<Null> updateTreatmentProduct(String treatmentProductId, UpdateTreatmentProductRequest request);

    @Operation(
        summary = "시술 상품 삭제",
        description = "시술 상품을 삭제합니다. 예약이 있는 시술 상품은 삭제할 수 없습니다.",
        parameters = {
            @Parameter(
                name = "treatmentProductId",
                description = "삭제할 시술 상품 ID",
                required = true,
                example = "product-123"
            )
        }
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "시술 상품 삭제 성공"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "시술 상품을 찾을 수 없음"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "예약이 있는 시술 상품은 삭제할 수 없음"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "시스템 기본 시술 상품(상담, 정기 검진)은 삭제할 수 없음"
        )
    })
    Api<Null> deleteTreatmentProduct(String treatmentProductId);
}