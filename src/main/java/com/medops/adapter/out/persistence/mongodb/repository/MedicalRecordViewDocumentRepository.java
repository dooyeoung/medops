package com.medops.adapter.out.persistence.mongodb.repository;

import com.medops.adapter.out.persistence.mongodb.document.MedicalRecordViewDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;

public interface MedicalRecordViewDocumentRepository extends MongoRepository<MedicalRecordViewDocument, String> {
    @Query("{ 'hospitalId': ?0, 'startTime': { '$gte': ?1 }, 'endTime': { '$lte': ?2 } }")
    List<MedicalRecordViewDocument> findAllByHospitalInRange(
        String hospitalId, Instant startTime, Instant endTime
    );

    List<MedicalRecordViewDocument> findAllByUserIdOrderByStartTimeDesc(String userId);
    List<MedicalRecordViewDocument> findAllByUserIdAndHospitalIdOrderByStartTimeDesc(String userId, String hospitalId);
}
