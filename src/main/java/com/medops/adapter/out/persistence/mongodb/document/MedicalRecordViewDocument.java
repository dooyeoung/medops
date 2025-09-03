package com.medops.adapter.out.persistence.mongodb.document;

import com.medops.domain.enums.MedicalRecordStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Document("medops_medical_record_view")
@AllArgsConstructor
public class MedicalRecordViewDocument {

    @Id
    private final String id;
    private final String userId;
    private final String userName;
    private final String hospitalId;
    private final String hospitalName;
    private final String doctorId;
    private final String doctorName;
    private final String note;
    private final MedicalRecordStatus status;
    private final Instant startTime;
    private final Instant endTime;
    private final String treatmentProductId;
    private final String treatmentProductName;
    private final String userMemo;

}
