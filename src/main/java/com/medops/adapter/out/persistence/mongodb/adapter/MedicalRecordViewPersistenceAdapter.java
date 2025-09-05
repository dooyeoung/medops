package com.medops.adapter.out.persistence.mongodb.adapter;

import com.medops.adapter.out.persistence.mongodb.document.MedicalRecordViewDocument;
import com.medops.adapter.out.persistence.mongodb.repository.MedicalRecordViewDocumentRepository;
import com.medops.application.port.out.LoadMedicalRecordViewPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MedicalRecordViewPersistenceAdapter implements LoadMedicalRecordViewPort {
    private final MedicalRecordViewDocumentRepository medicalRecordViewDocumentRepository;

    @Override
    public List<MedicalRecordViewDocument> loadMedicalRecordsByHospitalInRange(String hospitalId, Instant starTime, Instant endTime) {
        return medicalRecordViewDocumentRepository.findAllByHospitalInRange(hospitalId, starTime, endTime);
    }

    @Override
    public List<MedicalRecordViewDocument> loadMedicalRecordsByUserId(String userId) {
        return medicalRecordViewDocumentRepository.findAllByUserIdOrderByStartTimeDesc(userId);
    }

    @Override
    public List<MedicalRecordViewDocument> loadMedicalRecordsByUserIdAndByHospitalId(String userId, String hospitalId) {
        return medicalRecordViewDocumentRepository.findAllByUserIdAndHospitalIdOrderByStartTimeDesc(userId, hospitalId);
    }

    @Override
    public Optional<MedicalRecordViewDocument> loadMedicalRecordById(String recordId) {
        return medicalRecordViewDocumentRepository.findById(recordId);
    }

    @Override
    public List<MedicalRecordViewDocument> loadMedicalRecordsByTreatmentIdInRange(String treatmentProductId, Instant startTime, Instant endTime) {
        return medicalRecordViewDocumentRepository.findAllByTreatmentProductIdInRange(treatmentProductId, startTime, endTime);
    }
}
