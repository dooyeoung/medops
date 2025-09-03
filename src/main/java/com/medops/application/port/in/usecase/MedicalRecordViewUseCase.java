package com.medops.application.port.in.usecase;

import com.medops.adapter.out.persistence.mongodb.document.MedicalRecordViewDocument;

import java.time.Instant;
import java.util.List;

public interface MedicalRecordViewUseCase {
    List<MedicalRecordViewDocument> getMedicalRecordsByHospital(String hospitalId, Instant startTime, Instant endTime);
    List<MedicalRecordViewDocument> getMedicalRecordsByUser(String userId);
    List<MedicalRecordViewDocument> getMedicalRecordsByUserAndHospital(String userId, String hosptialId);
    MedicalRecordViewDocument getMedicalRecord(String recordId);
}
