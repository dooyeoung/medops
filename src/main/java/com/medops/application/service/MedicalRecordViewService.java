package com.medops.application.service;

import com.medops.adapter.out.persistence.mongodb.document.MedicalRecordViewDocument;
import com.medops.application.port.in.usecase.MedicalRecordViewUseCase;
import com.medops.application.port.out.LoadMedicalRecordViewPort;
import com.medops.common.exception.NotFoundResource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;


@Service
@RequiredArgsConstructor
public class MedicalRecordViewService implements MedicalRecordViewUseCase {
    private final LoadMedicalRecordViewPort loadMedicalRecordViewPort;

    @Override
    public List<MedicalRecordViewDocument> getMedicalRecordsByHospital(String hospitalId, Instant startTime, Instant endTime) {
        return loadMedicalRecordViewPort.loadMedicalRecordsByHospitalInRange(hospitalId, startTime, endTime);
    }

    @Override
    public List<MedicalRecordViewDocument> getMedicalRecordsByUser(String userId) {
        return loadMedicalRecordViewPort.loadMedicalRecordsByUserId(userId);
    }

    @Override
    public List<MedicalRecordViewDocument> getMedicalRecordsByUserAndHospital(String userId, String hospitalId) {
        return loadMedicalRecordViewPort.loadMedicalRecordsByUserIdAndByHospitalId(userId, hospitalId);
    }

    @Override
    public MedicalRecordViewDocument getMedicalRecord(String recordId) {
        return loadMedicalRecordViewPort.loadMedicalRecordById(recordId).orElseThrow(
            () -> new NotFoundResource("진료 기록 찾을수 없음")
        );
    }
}
