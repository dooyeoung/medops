package com.medops.domain.event;

import com.medops.domain.enums.MedicalRecordStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@AllArgsConstructor
@Builder
public class MedicalRecordEvent {
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
