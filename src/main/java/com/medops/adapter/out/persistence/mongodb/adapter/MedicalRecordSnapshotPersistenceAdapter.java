package com.medops.adapter.out.persistence.mongodb.adapter;

import com.medops.adapter.out.persistence.mongodb.converter.MedicalRecordSnapshotConverter;
import com.medops.adapter.out.persistence.mongodb.repository.MedicalRecordSnapshotDocumentRepository;
import com.medops.application.port.out.LoadMedicalRecordSnapshotPort;
import com.medops.domain.model.MedicalRecordSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MedicalRecordSnapshotPersistenceAdapter implements LoadMedicalRecordSnapshotPort {
    private final MedicalRecordSnapshotDocumentRepository medicalRecordSnapshotDocumentRepository;
    private final MedicalRecordSnapshotConverter medicalRecordSnapshotConverter;

    @Override
    public Optional<MedicalRecordSnapshot> loadMedicalRecordSnapshot(String id) {
        return medicalRecordSnapshotDocumentRepository.findLatestByRecordId(id).map(medicalRecordSnapshotConverter::toDomain);
    }
}
