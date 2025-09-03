package com.medops.application.eventsourcing.event;


import com.medops.domain.enums.MedicalRecordStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.Instant;

@Getter
public class ReservationCreated extends MedicalRecordEvent{
    private final String userId;
    private final String hospitalId;
    private final String treatmentProductId;
    private final Instant startTime;
    private final Instant endTime;
    private final String userMemo;
    private final String note;

    @JsonCreator
    public ReservationCreated(
        @JsonProperty("recordId") String recordId,
        @JsonProperty("status") MedicalRecordStatus status,
        @JsonProperty("userId") String userId,
        @JsonProperty("hospitalId") String hospitalId,
        @JsonProperty("treatmentProductId") String treatmentProductId,
        @JsonProperty("startTime") Instant startTime,
        @JsonProperty("endTime") Instant endTime,
        @JsonProperty("userMemo") String userMemo,
        @JsonProperty("note") String note
    ) {
        super(recordId, status);
        this.userId = userId;
        this.hospitalId = hospitalId;
        this.treatmentProductId = treatmentProductId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.userMemo = userMemo;
        this.note = note;
    }
}