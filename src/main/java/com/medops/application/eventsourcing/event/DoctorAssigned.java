package com.medops.application.eventsourcing.event;


import com.medops.domain.enums.MedicalRecordStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class DoctorAssigned extends MedicalRecordEvent{
    private final String doctorId;
    private final String doctorName;
    private final String adminId;
    private final String adminName;

    @JsonCreator
    public DoctorAssigned(
        @JsonProperty("recordId") String recordId,
        @JsonProperty("status") MedicalRecordStatus status,
        @JsonProperty("doctorId") String doctorId,
        @JsonProperty("doctorName") String doctorName,
        @JsonProperty("adminId") String adminId,
        @JsonProperty("adminName") String adminName
    ) {
        super(recordId, status);
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.adminId = adminId;
        this.adminName = adminName;
    }
}