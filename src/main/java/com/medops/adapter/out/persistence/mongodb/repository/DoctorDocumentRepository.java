package com.medops.adapter.out.persistence.mongodb.repository;

import com.medops.adapter.out.persistence.mongodb.document.DoctorDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DoctorDocumentRepository extends MongoRepository<DoctorDocument, String> {

    List<DoctorDocument> findAllByHospitalId(String hospitalId);
}
