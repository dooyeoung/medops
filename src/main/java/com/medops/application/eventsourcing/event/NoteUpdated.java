package com.medops.application.eventsourcing.event;


import com.medops.domain.enums.MedicalRecordStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class NoteUpdated extends MedicalRecordEvent{
    private String note;

    @JsonCreator
    public NoteUpdated(
        @JsonProperty("recordId") String recordId,
        @JsonProperty("status") MedicalRecordStatus status,
        @JsonProperty("note") String note
    ) {
        super(recordId, status);
        this.note = note;
    }
}