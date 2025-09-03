package com.medops.adapter.in.web.controller;

import com.medops.adapter.in.annotation.AdminSession;
import com.medops.adapter.in.annotation.UserSession;
import com.medops.adapter.in.web.request.*;
import com.medops.adapter.out.persistence.mongodb.document.MedicalRecordViewDocument;
import com.medops.application.eventsourcing.command.*;
import com.medops.application.eventsourcing.processor.MedicalRecordCommandProcessor;
import com.medops.application.port.in.usecase.MedicalRecordEventUseCase;
import com.medops.application.port.in.usecase.MedicalRecordViewUseCase;
import com.medops.common.response.Api;
import com.medops.domain.model.Admin;
import com.medops.domain.event.MedicalRecordEvent;
import com.medops.domain.model.User;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/medical-records")
public class MedicalRecordApiController implements MedicalRecordApiControllerSpec {
    private final MedicalRecordCommandProcessor commandProcessor;
    private final MedicalRecordViewUseCase medicalRecordViewUseCase;
    private final MedicalRecordEventUseCase medicalRecordEventUseCase;

    @GetMapping("/{recordId}")
    public Api<MedicalRecordViewDocument> getMedicalRecord(
        @PathVariable String recordId
    ){
        return Api.OK(medicalRecordViewUseCase.getMedicalRecord(recordId));
    }

    @GetMapping("/{recordId}/events")
    public Api<List<MedicalRecordEvent>> getEventsByRecordId(
        @PathVariable String recordId
    ){
        return Api.OK(medicalRecordEventUseCase.getEventsByRecordId(recordId));
    }

    @PostMapping
    public Api<Null> createMedicalRecord(
        @Parameter(hidden = true)
        @UserSession User user,
        @RequestBody CreateReservationRequest request
    ) {
        var command = new ReservationCreatedCommand(
            UUID.randomUUID().toString(),
            user.getId(),
            request.hospitalId(),
            request.treatmentProductId(),
            request.startTime(),
            request.endTime(),
            request.userMemo(),
            ""
        );
        commandProcessor.handle(command);
        return Api.OK(null);
    }

    @PostMapping("/follow-up")
    public Api<Null> followUpMedicalRecord(
        @Parameter(hidden = true)
        @AdminSession Admin admin,
        @RequestBody FollowUpReservationRequest request
    ) {
        var command = new ReservationCreatedCommand(
            UUID.randomUUID().toString(),
            request.userId(),
            request.hospitalId(),
            request.treatmentProductId(),
            request.startTime(),
            request.endTime(),
            "",
            request.note()
        );
        commandProcessor.handle(command);
        return Api.OK(null);
    }


    @GetMapping("/hospitals/{hospitalId}")
    public Api<List<MedicalRecordViewDocument>> getHospitalMedicalRecords(
        @PathVariable String hospitalId,
        @RequestParam Instant startTime,
        @RequestParam Instant endTime
    ){
        return Api.OK(medicalRecordViewUseCase.getMedicalRecordsByHospital(hospitalId, startTime, endTime));
    }

    @PatchMapping("/{recordId}/status/confirm")
    public Api<Null> confirmReservation(
        @PathVariable String recordId,
        @RequestBody ConfirmReservationRequest request
    ){
        ConfirmCommand command = new ConfirmCommand(
            recordId,
            request.userId(),
            request.hospitalId(),
            request.adminId()
        );
        commandProcessor.handle(command);
        return Api.OK(null);
    }

    @PatchMapping("/{recordId}/status/pending")
    public Api<Null> pendingReservation(
        @PathVariable String recordId,
        @RequestBody PendingReservationRequest request
    ){
        PendingCommand command = new PendingCommand(
            recordId,
            request.userId(),
            request.hospitalId(),
            request.adminId()
        );
        commandProcessor.handle(command);
        return Api.OK(null);
    }

    @PatchMapping("/{recordId}/status/cancel")
    public Api<Null> cancelReservation(
        @PathVariable String recordId,
        @RequestBody CancelReservationRequest request
    ){
        CancelCommand command = new CancelCommand(
            recordId,
            request.userId(),
            request.hospitalId(),
            request.adminId()
        );
        commandProcessor.handle(command);
        return Api.OK(null);
    }

    @PatchMapping("/{recordId}/status/complete")
    public Api<Null> completeReservation(
        @PathVariable String recordId,
        @RequestBody CompleteReservationRequest request
    ){
        CompleteCommand command = new CompleteCommand(
            recordId,
            request.userId(),
            request.hospitalId(),
            request.adminId()
        );
        commandProcessor.handle(command);
        return Api.OK(null);
    }

    @PatchMapping("/{recordId}/note")
    public Api<Null> updateNote(
        @PathVariable String recordId,
        @RequestBody UpdateReservationNoteRequest request
    ){
        NoteUpdateCommand command = new NoteUpdateCommand(
            recordId,
            request.userId(),
            request.hospitalId(),
            request.note()
        );
        commandProcessor.handle(command);
        return Api.OK(null);
    }


    @GetMapping("/users/{userId}")
    public Api<List<MedicalRecordViewDocument>> getUserMedicalRecords(
        @PathVariable String userId
    ){
        return Api.OK(medicalRecordViewUseCase.getMedicalRecordsByUser(userId));
    }

    @GetMapping("/users/{userId}/hospitals/{hospitalId}")
    public Api<List<MedicalRecordViewDocument>> getUserMedicalRecordsInHospital(
        @PathVariable String userId,
        @PathVariable String hospitalId
    ){
        return Api.OK(medicalRecordViewUseCase.getMedicalRecordsByUserAndHospital(userId, hospitalId));
    }

    @PatchMapping("/{recordId}/doctor")
    public Api<Null> assignDoctorReservation(
        @AdminSession Admin admin,
        @PathVariable String recordId,
        @RequestBody AssignDoctorRequest request
    ){
        DoctorAssignCommand command = new DoctorAssignCommand(
            recordId,
            request.userId(),
            request.hospitalId(),
            request.doctorId(),
            admin.getId()
        );
        commandProcessor.handle(command);
        return Api.OK(null);
    }
}
