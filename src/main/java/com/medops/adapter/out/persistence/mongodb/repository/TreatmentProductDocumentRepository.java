package com.medops.adapter.out.persistence.mongodb.repository;

import com.medops.adapter.out.persistence.mongodb.document.TreatmentProductDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TreatmentProductDocumentRepository extends MongoRepository<TreatmentProductDocument, String> {
    List<TreatmentProductDocument> findAllByHospitalId(String hospitalId);
    List<TreatmentProductDocument> findByHospitalIdIn(List<String> hospitalIds);
}
