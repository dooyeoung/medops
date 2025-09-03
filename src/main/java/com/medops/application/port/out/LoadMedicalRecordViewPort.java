package com.medops.application.port.out;

import com.medops.adapter.out.persistence.mongodb.document.MedicalRecordViewDocument;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LoadMedicalRecordViewPort {
    List<MedicalRecordViewDocument> loadMedicalRecordsByHospitalInRange(String hospitalId, Instant starTime, Instant endTime);
    List<MedicalRecordViewDocument> loadMedicalRecordsByUserId(String userId);
    List<MedicalRecordViewDocument> loadMedicalRecordsByUserIdAndByHospitalId(String userId, String hospitalId);
    Optional<MedicalRecordViewDocument> loadMedicalRecordById(String recordId);
}
