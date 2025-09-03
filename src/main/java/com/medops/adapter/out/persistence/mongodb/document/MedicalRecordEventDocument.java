package com.medops.adapter.out.persistence.mongodb.document;

import com.medops.domain.enums.MedicalRecordStatus;
import lombok.Getter;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Getter
@Document("medops_medical_record_events")
@AllArgsConstructor
public class MedicalRecordEventDocument {
    @Id
    private String id;
    private String recordId;
    private Instant createdAt;
    private String eventType;

    private String hospitalId;
    private String userId;
    private Integer version;

    private MedicalRecordStatus status;

    private Map<String, Object> payload;
}