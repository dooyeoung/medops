package com.medops.adapter.out.persistence.mongodb.adapter;

import com.medops.adapter.out.persistence.mongodb.converter.MedicalRecordEventConverter;
import com.medops.adapter.out.persistence.mongodb.repository.MedicalRecordEventDocumentRepository;
import com.medops.application.port.out.LoadMedicalRecordEventPort;
import com.medops.domain.event.MedicalRecordEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MedicalRecordEventPersistenceAdapter implements LoadMedicalRecordEventPort {
    private final MedicalRecordEventDocumentRepository medicalRecordEventDocumentRepository;
    private final MedicalRecordEventConverter medicalRecordEventConverter;

    @Override
    public List<MedicalRecordEvent> loadEventsByRecordId(String recordId) {
        return medicalRecordEventDocumentRepository.getAllByRecordId(recordId).stream().map(medicalRecordEventConverter::toDomain).toList();
    }
}
