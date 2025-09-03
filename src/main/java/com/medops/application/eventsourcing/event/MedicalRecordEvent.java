package com.medops.application.eventsourcing.event;

import com.medops.domain.enums.MedicalRecordStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public abstract class MedicalRecordEvent {
    private final String recordId;
    private final MedicalRecordStatus status;

    @JsonCreator
    protected MedicalRecordEvent(
        @JsonProperty("recordId") String recordId,
        @JsonProperty("status") MedicalRecordStatus status
    ) {
        this.recordId = recordId;
        this.status = status;
    }
}
