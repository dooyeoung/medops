package com.medops.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class MedicalRecordSnapshot {
    private String id;
    private String recordId;
    private Instant createdAt;
    private Integer version;

    private MedicalRecord state;

    public static MedicalRecordSnapshot seed(String recordId, MedicalRecord seedState) {
        return new MedicalRecordSnapshot(
            UUID.randomUUID().toString(),
            recordId,
            Instant.now(),
            0,
            seedState
        );
    }
}
