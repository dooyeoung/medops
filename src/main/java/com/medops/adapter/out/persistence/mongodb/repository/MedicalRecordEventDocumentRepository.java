package com.medops.adapter.out.persistence.mongodb.repository;

import com.medops.adapter.out.persistence.mongodb.document.MedicalRecordEventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MedicalRecordEventDocumentRepository extends MongoRepository<MedicalRecordEventDocument, String> {
    List<MedicalRecordEventDocument> findAllByRecordIdAndVersionGreaterThanEqualOrderByVersionAsc(String recordId, Integer version);

    List<MedicalRecordEventDocument> getAllByRecordId(String recordId);
}
