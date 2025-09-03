package com.medops.adapter.out.persistence.mongodb.document;

import com.medops.domain.enums.MedicalRecordStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Document("medops_medical_record_snapshot")
@AllArgsConstructor
public class MedicalRecordSnapshotDocument {

    @Id
    private final String id;
    private final String recordId;
    private final Instant createdAt;
    private final Integer version;

    private final String userId;
    private final String hospitalId;
    private final String doctor;
    private final String note;
    private final MedicalRecordStatus status;

    private ReservationDocumentComponent reservation;
}

