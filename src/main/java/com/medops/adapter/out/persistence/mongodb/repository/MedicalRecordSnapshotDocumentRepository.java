package com.medops.adapter.out.persistence.mongodb.repository;

import com.medops.adapter.out.persistence.mongodb.document.MedicalRecordSnapshotDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface MedicalRecordSnapshotDocumentRepository extends MongoRepository<MedicalRecordSnapshotDocument, String> {
    Optional<MedicalRecordSnapshotDocument> findTopByRecordIdOrderByVersionDesc(String recordId);

    Optional<MedicalRecordSnapshotDocument> findLatestByRecordId(String recordId);
}
