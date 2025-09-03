package com.medops.adapter.in.web.controller;

import com.medops.adapter.in.web.request.*;
import com.medops.adapter.out.persistence.mongodb.document.MedicalRecordViewDocument;
import com.medops.common.response.Api;
import com.medops.domain.model.Admin;
import com.medops.domain.event.MedicalRecordEvent;
import com.medops.domain.model.User;
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

import java.time.Instant;
import java.util.List;

@Tag(name = "진료 기록 API", description = "진료 예약 및 기록 관리 API")
public interface MedicalRecordApiControllerSpec {

    @Operation(
        summary = "진료 기록 상세 조회",
        description = "특정 진료 기록의 상세 정보를 조회합니다.",
        parameters = {
            @Parameter(
                name = "recordId",
                description = "조회할 진료 기록 ID",
                required = true,
                example = "record-123"
            )
        }
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "진료 기록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MedicalRecordViewDocument.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "진료 기록을 찾을 수 없음")
    })
    Api<MedicalRecordViewDocument> getMedicalRecord(String recordId);

    @Operation(
        summary = "진료 기록 이벤트 히스토리 조회",
        description = "특정 진료 기록에 대한 모든 변경 이력(이벤트)을 조회합니다.",
        parameters = {
            @Parameter(
                name = "recordId",
                description = "진료 기록 ID",
                required = true,
                example = "record-123"
            )
        }
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "이벤트 히스토리 조회 성공",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = MedicalRecordEvent.class))
            )
        ),
        @ApiResponse(responseCode = "404", description = "진료 기록을 찾을 수 없음")
    })
    Api<List<MedicalRecordEvent>> getEventsByRecordId(String recordId);

    @Operation(
        summary = "병원 진료 기록 목록 조회",
        description = "특정 병원의 지정된 기간 내 모든 진료 기록을 조회합니다.",
        parameters = {
            @Parameter(
                name = "hospitalId",
                description = "병원 ID",
                required = true,
                example = "hospital-123"
            ),
            @Parameter(
                name = "startTime",
                description = "조회 시작 시간 (ISO 8601)",
                required = true,
                example = "2023-12-01T00:00:00Z"
            ),
            @Parameter(
                name = "endTime",
                description = "조회 종료 시간 (ISO 8601)",
                required = true,
                example = "2023-12-31T23:59:59Z"
            )
        }
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "병원 진료 기록 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = MedicalRecordViewDocument.class))
            )
        ),
        @ApiResponse(responseCode = "404", description = "병원을 찾을 수 없음")
    })
    Api<List<MedicalRecordViewDocument>> getHospitalMedicalRecords(
        String hospitalId, 
        Instant startTime, 
        Instant endTime
    );

    @Operation(
        summary = "사용자 진료 기록 목록 조회",
        description = "특정 사용자의 모든 진료 기록을 조회합니다.",
        parameters = {
            @Parameter(
                name = "userId",
                description = "사용자 ID",
                required = true,
                example = "user-123"
            )
        }
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "사용자 진료 기록 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = MedicalRecordViewDocument.class))
            )
        ),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    Api<List<MedicalRecordViewDocument>> getUserMedicalRecords(String userId);

    @Operation(
        summary = "사용자의 특정 병원 진료 기록 조회",
        description = "특정 사용자의 특정 병원에서의 진료 기록을 조회합니다.",
        parameters = {
            @Parameter(
                name = "userId",
                description = "사용자 ID",
                required = true,
                example = "user-123"
            ),
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
            description = "사용자의 병원별 진료 기록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = MedicalRecordViewDocument.class))
            )
        ),
        @ApiResponse(responseCode = "404", description = "사용자 또는 병원을 찾을 수 없음")
    })
    Api<List<MedicalRecordViewDocument>> getUserMedicalRecordsInHospital(
        String userId, 
        String hospitalId
    );

    @Operation(
        summary = "예약 생성 (환자용)",
        description = "환자가 직접 예약을 생성합니다. 인증된 사용자 세션이 필요합니다.",
        requestBody = @RequestBody(
            description = "예약 생성 요청",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CreateReservationRequest.class)
            )
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "예약 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "409", description = "예약 시간 충돌")
    })
    Api<Null> createMedicalRecord(User user, CreateReservationRequest request);

    @Operation(
        summary = "후속 예약 생성 (관리자용)",
        description = "관리자가 환자를 대신하여 후속 예약을 생성합니다.",
        requestBody = @RequestBody(
            description = "후속 예약 생성 요청",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FollowUpReservationRequest.class)
            )
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "후속 예약 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "관리자 권한 필요"),
        @ApiResponse(responseCode = "409", description = "예약 시간 충돌")
    })
    Api<Null> followUpMedicalRecord(Admin admin, FollowUpReservationRequest request);

    @Operation(
        summary = "예약 확정",
        description = "대기 중인 예약을 확정 상태로 변경합니다.",
        parameters = {
            @Parameter(
                name = "recordId",
                description = "예약 ID",
                required = true,
                example = "record-123"
            )
        },
        requestBody = @RequestBody(
            description = "예약 확정 요청",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConfirmReservationRequest.class)
            )
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "예약 확정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 상태 변경"),
        @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
    })
    Api<Null> confirmReservation(String recordId, ConfirmReservationRequest request);

    @Operation(
        summary = "예약 대기로 변경",
        description = "예약을 대기 중 상태로 변경합니다.",
        parameters = {
            @Parameter(
                name = "recordId",
                description = "예약 ID",
                required = true,
                example = "record-123"
            )
        },
        requestBody = @RequestBody(
            description = "예약 대기 요청",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PendingReservationRequest.class)
            )
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "예약 대기 변경 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 상태 변경"),
        @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
    })
    Api<Null> pendingReservation(String recordId, PendingReservationRequest request);

    @Operation(
        summary = "예약 취소",
        description = "예약을 취소 상태로 변경합니다.",
        parameters = {
            @Parameter(
                name = "recordId",
                description = "예약 ID",
                required = true,
                example = "record-123"
            )
        },
        requestBody = @RequestBody(
            description = "예약 취소 요청",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CancelReservationRequest.class)
            )
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "예약 취소 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 상태 변경"),
        @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
    })
    Api<Null> cancelReservation(String recordId, CancelReservationRequest request);

    @Operation(
        summary = "예약 완료",
        description = "예약을 완료 상태로 변경합니다. 진료가 완료되었음을 의미합니다.",
        parameters = {
            @Parameter(
                name = "recordId",
                description = "예약 ID",
                required = true,
                example = "record-123"
            )
        },
        requestBody = @RequestBody(
            description = "예약 완료 요청",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CompleteReservationRequest.class)
            )
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "예약 완료 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 상태 변경"),
        @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
    })
    Api<Null> completeReservation(String recordId, CompleteReservationRequest request);

    @Operation(
        summary = "예약 메모 수정",
        description = "예약에 대한 메모를 수정합니다.",
        parameters = {
            @Parameter(
                name = "recordId",
                description = "예약 ID",
                required = true,
                example = "record-123"
            )
        },
        requestBody = @RequestBody(
            description = "메모 수정 요청",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UpdateReservationNoteRequest.class)
            )
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "메모 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
    })
    Api<Null> updateNote(String recordId, UpdateReservationNoteRequest request);

    @Operation(
        summary = "담당 의사 배정",
        description = "예약에 담당 의사를 배정합니다. 관리자 권한이 필요합니다.",
        parameters = {
            @Parameter(
                name = "recordId",
                description = "예약 ID",
                required = true,
                example = "record-123"
            )
        },
        requestBody = @RequestBody(
            description = "의사 배정 요청",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssignDoctorRequest.class)
            )
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "의사 배정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "관리자 권한 필요"),
        @ApiResponse(responseCode = "404", description = "예약 또는 의사를 찾을 수 없음")
    })
    Api<Null> assignDoctorReservation(Admin admin, String recordId, AssignDoctorRequest request);
}