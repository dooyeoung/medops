package com.medops.application.port.out;

import com.medops.domain.model.MedicalRecordSnapshot;

import java.util.Optional;

public interface LoadMedicalRecordSnapshotPort {
    Optional<MedicalRecordSnapshot> loadMedicalRecordSnapshot(String id);
}
