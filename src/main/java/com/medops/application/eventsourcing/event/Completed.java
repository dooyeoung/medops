package com.medops.application.eventsourcing.event;

import com.medops.domain.enums.MedicalRecordStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Completed extends MedicalRecordEvent {
    private final String adminId;
    private final String adminName;

    @JsonCreator
    public Completed(
        @JsonProperty("recordId") String recordId,
        @JsonProperty("adminId") String adminId,
        @JsonProperty("adminName") String adminName
    ) {
        super(recordId, MedicalRecordStatus.RESERVED);
        this.adminId = adminId;
        this.adminName = adminName;
    }
}
